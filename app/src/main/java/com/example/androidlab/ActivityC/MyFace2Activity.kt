package com.example.androidlab.ActivityC

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.androidlab.R
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions


class MyFace2Activity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var countdownText: TextView
    private var imageCapture: ImageCapture? = null

    private lateinit var answer_emotionList: ArrayList<String>
    private lateinit var my_emotionList: ArrayList<String>

    private lateinit var capturedImageView: ImageView


    private val emotionLabels = listOf("분노", "기쁨", "무표정", "슬픔", "놀람")
    private lateinit var interpreter: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_face_2)

        capturedImageView = findViewById(R.id.capturedImageView)


        answer_emotionList = intent.getStringArrayListExtra("answer_emotionList") ?: arrayListOf()
        my_emotionList = intent.getStringArrayListExtra("my_emotionList") ?: arrayListOf()

        interpreter = Interpreter(loadModelFile(this, "efficientnet_b0.tflite"))


        previewView = findViewById(R.id.previewView)
        countdownText = findViewById(R.id.countdownText)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
            startCamera()
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            preview.setSurfaceProvider(previewView.surfaceProvider)

            imageCapture = ImageCapture.Builder().build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            startCountdownAndCapture()

        }, ContextCompat.getMainExecutor(this))
    }

    private fun startCountdownAndCapture() {
        countdownText.visibility = View.VISIBLE
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownText.text = "${millisUntilFinished / 1000 + 1}"
            }

            override fun onFinish() {
                countdownText.visibility = View.GONE
                takePhoto()
            }
        }.start()
    }

    private fun takePhoto() {
        val photoFile = File(getExternalFilesDir(null), "selfie_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val photoUri = Uri.fromFile(photoFile)
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri))
                    val correctedBitmap = rotateBitmapIfRequired(this@MyFace2Activity, bitmap, photoUri)

                    cropFaceFromBitmap(correctedBitmap) { faceBitmap ->
                        // faceBitmap = 얼굴만 잘라낸 비트맵
                        capturedImageView.setImageBitmap(faceBitmap)
                    }



                    // 캡처한 이미지를 화면에 띄우기
                    capturedImageView.setImageBitmap(correctedBitmap)
                    capturedImageView.visibility = View.VISIBLE

                    // 5초 대기 후 감정 예측 실행
                    Handler(Looper.getMainLooper()).postDelayed({
                        capturedImageView.visibility = View.GONE

                        val input = preprocessBitmap(correctedBitmap)
                        val output = Array(1) { FloatArray(5) }

                        interpreter.run(input, output)

                        val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: 0
                        val emotion = emotionLabels[maxIdx]

                        my_emotionList.add(emotion)

                        val intent = Intent(this@MyFace2Activity, MyFace3Activity::class.java)
                        intent.putStringArrayListExtra("answer_emotionList", answer_emotionList)
                        intent.putStringArrayListExtra("my_emotionList", my_emotionList)
                        startActivity(intent)
                        finish()
                    }, 1000) // 5초
                }


                override fun onError(exception: ImageCaptureException) {
                }
            }
        )
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fd = context.assets.openFd(modelName)
        val input = FileInputStream(fd.fileDescriptor)
        return input.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }

    private fun preprocessBitmap(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        // [1][3][128][128]
        val input = Array(1) { Array(3) { Array(224) { FloatArray(224) } } }

        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resized.getPixel(x, y)

                // channel-first: [1][C][H][W]
                input[0][0][y][x] = Color.red(pixel) / 255f   // R
                input[0][1][y][x] = Color.green(pixel) / 255f // G
                input[0][2][y][x] = Color.blue(pixel) / 255f  // B
            }
        }

        return input
    }
    private fun rotateBitmapIfRequired(context: Context, bitmap: Bitmap, uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        val exif = ExifInterface(inputStream!!)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        inputStream.close()

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun cropFaceFromBitmap(bitmap: Bitmap, onFaceCropped: (Bitmap) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    // 첫 번째 얼굴만 사용
                    val face = faces[0]
                    val bounds = face.boundingBox

                    // 이미지 범위를 넘어가지 않도록 보정
                    val left = bounds.left.coerceAtLeast(0)
                    val top = bounds.top.coerceAtLeast(0)
                    val right = bounds.right.coerceAtMost(bitmap.width)
                    val bottom = bounds.bottom.coerceAtMost(bitmap.height)

                    val width = right - left
                    val height = bottom - top

                    if (width > 0 && height > 0) {
                        val croppedFace = Bitmap.createBitmap(bitmap, left, top, width, height)
                        onFaceCropped(croppedFace)
                    }
                }
            }
            .addOnFailureListener {
                Log.e("FaceCrop", "Face detection failed", it)
            }
    }


}