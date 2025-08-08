package com.example.androidlab.ActivityB

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.util.Size
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
import com.example.androidlab.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class YourFace1Activity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var faceThumbnail: ImageView
    private lateinit var emotionTextView: TextView

    private val emotionLabels = listOf("분노", "기쁨", "무표정", "슬픔", "놀람")
    private val vibrationPatterns = mapOf(
        "기쁨" to longArrayOf(0, 100, 50, 100),
        "슬픔" to longArrayOf(0, 300),
        "분노" to longArrayOf(0, 200, 100, 200),
        "놀람" to longArrayOf(0, 50, 50, 50, 50, 50),
        "무표정" to longArrayOf()
    )

    private lateinit var interpreter: Interpreter
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraProvider: ProcessCameraProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_face_1)

        previewView = findViewById(R.id.previewView)
        faceThumbnail = findViewById(R.id.faceThumbnail)
        emotionTextView = findViewById(R.id.emotionTextView)

        interpreter = Interpreter(loadModelFile(this, "best.tflite"))

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        } else {
            startCamera()
        }

        handler = Handler(Looper.getMainLooper())
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(640, 480))
                .build()

            preview.setSurfaceProvider(previewView.surfaceProvider)
            cameraProvider.unbindAll()

            // camera 인스턴스 확보
            val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            val zoomStateLiveData = camera.cameraInfo.zoomState

            zoomStateLiveData.observe(this) { zoomState ->
                val minZoom = zoomState.minZoomRatio
                val maxZoom = zoomState.maxZoomRatio
                Log.d("ZoomRange", "지원 줌 배율: 최소 = $minZoom, 최대 = $maxZoom")
            }


            // ✅ 줌 배율 0.5배 설정
            camera.cameraControl.setZoomRatio(0.6f)

            startPeriodicCapture()
        }, ContextCompat.getMainExecutor(this))
    }


    private fun startPeriodicCapture() {
        runnable = object : Runnable {
            override fun run() {
                takePhoto()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun takePhoto() {
        val photoFile = File.createTempFile("selfie", ".jpg", cacheDir)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    val rotatedBitmap = rotateBitmapIfRequired(this@YourFace1Activity, bitmap, Uri.fromFile(photoFile))

                    cropFaceFromBitmap(rotatedBitmap) { faceBitmap ->
                        if (faceBitmap != null) {
                            faceThumbnail.setImageBitmap(faceBitmap)

                            val input = preprocessBitmap(faceBitmap)
                            val output = Array(1) { FloatArray(5) }
                            interpreter.run(input, output)

                            val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: 0
                            val emotion = emotionLabels[maxIdx]

                            emotionTextView.text = "감정: $emotion"
                            vibrate(emotion)
                        } else {
                            // 얼굴 인식 실패한 경우
                            faceThumbnail.setImageDrawable(null)
                            emotionTextView.text = "감정: -"
                        }
                    }

                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("Capture", "Error capturing image", exception)
                }
            }
        )
    }

    private fun vibrate(emotion: String) {
        val pattern = vibrationPatterns[emotion] ?: return

        // 👇 패턴이 비어있으면 진동 생략
        if (pattern.isEmpty()) return

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }


    private fun preprocessBitmap(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val resized = Bitmap.createScaledBitmap(bitmap, 256, 256, true)

        // [1][3][128][128]
        val input = Array(1) { Array(3) { Array(256) { FloatArray(256) } } }

        for (y in 0 until 256) {
            for (x in 0 until 256) {
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

    private fun cropFaceFromBitmap(bitmap: Bitmap, onFaceCropped: (Bitmap?) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0]
                    val bounds = face.boundingBox

                    val left = bounds.left.coerceAtLeast(0)
                    val top = bounds.top.coerceAtLeast(0)
                    val right = bounds.right.coerceAtMost(bitmap.width)
                    val bottom = bounds.bottom.coerceAtMost(bitmap.height)

                    val width = right - left
                    val height = bottom - top

                    if (width > 0 && height > 0) {
                        val croppedFace = Bitmap.createBitmap(bitmap, left, top, width, height)
                        onFaceCropped(croppedFace)
                        return@addOnSuccessListener
                    }
                }

                // 얼굴이 없거나 실패한 경우 null 전달
                onFaceCropped(null)
            }
            .addOnFailureListener {
                Log.e("FaceCrop", "Face detection failed", it)
                onFaceCropped(null) // 실패한 경우도 썸네일 비우기
            }
    }


    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fd = context.assets.openFd(modelName)
        val input = FileInputStream(fd.fileDescriptor)
        return input.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }
}