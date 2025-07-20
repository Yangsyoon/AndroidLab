package com.example.androidlab.ActivityC

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
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
import java.io.File
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MyFace2Activity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var countdownText: TextView
    private var imageCapture: ImageCapture? = null

    private lateinit var answer_emotionList: ArrayList<String>
    private lateinit var my_emotionList: ArrayList<String>

    private val emotionLabels = listOf("분노", "혐오", "두려움", "기쁨", "슬픔", "놀람", "무표정")
    private lateinit var interpreter: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_face_2)

        answer_emotionList = intent.getStringArrayListExtra("answer_emotionList") ?: arrayListOf()
        my_emotionList = intent.getStringArrayListExtra("my_emotionList") ?: arrayListOf()

        interpreter = Interpreter(loadModelFile(this, "emotion_model.tflite"))


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
                    val input = preprocessBitmap(bitmap)
                    val output = Array(1) { FloatArray(7) }

                    interpreter.run(input, output)

                    val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: 0
                    val emotion = emotionLabels[maxIdx]

                    my_emotionList.add(emotion)

                    val intent = Intent(this@MyFace2Activity, MyFace3Activity::class.java)
                    intent.putStringArrayListExtra("answer_emotionList", answer_emotionList)
                    intent.putStringArrayListExtra("my_emotionList", my_emotionList)
                    startActivity(intent)
                    finish()
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
}