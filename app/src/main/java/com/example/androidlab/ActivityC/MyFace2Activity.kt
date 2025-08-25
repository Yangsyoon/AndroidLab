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
import com.example.androidlab.BaseActivity
import com.example.androidlab.R
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions


class MyFace2Activity : BaseActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var countdownText: TextView
    private var imageCapture: ImageCapture? = null

    private lateinit var answer_emotionList: ArrayList<String>
    private lateinit var my_emotionList: ArrayList<String>

    private lateinit var capturedImageView: ImageView


    private val emotionLabels = listOf("화남", "기쁨", "무표정", "놀람","슬픔")
    private lateinit var interpreter: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_face_2)

        capturedImageView = findViewById(R.id.capturedImageView)


        answer_emotionList = intent.getStringArrayListExtra("answer_emotionList") ?: arrayListOf()
        my_emotionList = intent.getStringArrayListExtra("my_emotionList") ?: arrayListOf()

        interpreter = Interpreter(loadModelFile(this, "best_efficientnet_b0_emotion.tflite"))


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



                    // 5초 대기 후 감정 예측 실행
                    Handler(Looper.getMainLooper()).postDelayed({
                        cropFaceFromBitmap(correctedBitmap) { faceBitmap ->
                            // 얼굴만 잘라낸 비트맵 화면에 표시
                            capturedImageView.setImageBitmap(faceBitmap)
                            //capturedImageView.visibility = View.VISIBLE

                            // 5초 대기 후 감정 예측 실행
                            Handler(Looper.getMainLooper()).postDelayed({
                                capturedImageView.visibility = View.GONE

                                val input = preprocessBitmap(faceBitmap)
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
                            }, 0)
                        }

                    }, 0)
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
        val imsize = 256
        val resized = Bitmap.createScaledBitmap(bitmap, imsize, imsize, true)

        // 좌우반전 (원하면 주석 해제)
        val matrix = Matrix().apply { postScale(-1f, 1f, imsize / 2f, imsize / 2f) }
        val flipped = Bitmap.createBitmap(resized, 0, 0, imsize, imsize, matrix, true)
        val processedBitmap = flipped.copy(Bitmap.Config.ARGB_8888, true)

        // PyTorch 기준: mean/std 값
        val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
        val std = floatArrayOf(0.229f, 0.224f, 0.225f)

        val input = Array(1) { Array(3) { Array(imsize) { FloatArray(imsize) } } }

        for (y in 0 until imsize) {
            for (x in 0 until imsize) {
                val pixel = processedBitmap.getPixel(x, y)

                val r = Color.red(pixel) / 255f
                val g = Color.green(pixel) / 255f
                val b = Color.blue(pixel) / 255f


                input[0][0][y][x] = (r - mean[0]) / std[0]  // R
                input[0][1][y][x] = (g - mean[1]) / std[1]  // G
                input[0][2][y][x] = (b - mean[2]) / std[2]  // B
            }
        }

        // 전처리된 비트맵 화면에 출력 (UI 스레드에서 실행)
        runOnUiThread {
            capturedImageView.setImageBitmap(processedBitmap)
            //capturedImageView.visibility = View.VISIBLE

            // 1초 대기 후 처리
            Handler(Looper.getMainLooper()).postDelayed({
                // 1초 후에 실행할 코드
                Log.d("Preprocess", "1초 대기 완료")
            }, 0)
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