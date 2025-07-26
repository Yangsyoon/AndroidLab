package com.example.androidlab.ActivityB

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.androidlab.R
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.Executors

class YourFace1Activity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var emotionText: TextView
    private lateinit var vibrator: Vibrator
    private lateinit var interpreter: Interpreter

    private val emotionLabels = listOf("분노", "혐오", "두려움", "기쁨", "슬픔", "놀람", "무표정")
    private val vibrationPatterns = mapOf(
        "기쁨" to longArrayOf(0, 100, 50, 100),
        "슬픔" to longArrayOf(0, 300),
        "분노" to longArrayOf(0, 200, 100, 200),
        "놀람" to longArrayOf(0, 50, 50, 50, 50, 50),
        "혐오" to longArrayOf(0, 100, 100, 200, 100, 100),
        "두려움" to longArrayOf(0, 300, 200, 150, 100),
        "무표정" to longArrayOf()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_face_1)

        previewView = findViewById(R.id.previewView)
        emotionText = findViewById(R.id.emotionTextView)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        interpreter = Interpreter(loadModelFile(this, "emotion_model.tflite"))

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor(), EmotionAnalyzer())
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA // 후면 카메라


            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    inner class EmotionAnalyzer : ImageAnalysis.Analyzer {
        private var lastAnalyzedTime = 0L

        override fun analyze(imageProxy: ImageProxy) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastAnalyzedTime >= 1000) {
                lastAnalyzedTime = currentTime

                val bitmap = imageProxy.toBitmap()
                val input = preprocessBitmap(bitmap)
                val output = Array(1) { FloatArray(7) }
                interpreter.run(input, output)

                val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: 0
                val emotion = emotionLabels[maxIdx]

                runOnUiThread {
                    emotionText.text = "감정: $emotion"
                    val pattern = vibrationPatterns[emotion]
                    if (pattern != null && pattern.isNotEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(pattern, -1)
                        }
                    }
                }
            }
            imageProxy.close()
        }
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun preprocessBitmap(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val resized = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        val result = Array(1) { Array(64) { Array(64) { FloatArray(3) } } }

        for (y in 0 until 64) {
            for (x in 0 until 64) {
                val pixel = resized.getPixel(x, y)
                result[0][y][x][0] = Color.red(pixel) / 255f
                result[0][y][x][1] = Color.green(pixel) / 255f
                result[0][y][x][2] = Color.blue(pixel) / 255f
            }
        }
        return result
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fd = context.assets.openFd(modelName)
        val input = FileInputStream(fd.fileDescriptor)
        return input.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }
}