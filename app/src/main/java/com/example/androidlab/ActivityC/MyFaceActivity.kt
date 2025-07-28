package com.example.androidlab.ActivityC

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.R

class MyFaceActivity : AppCompatActivity() {

    private val emotionLabels = listOf("분노", "기쁨", "무표정", "슬픔", "놀람")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_face)

        val fullList = List(1) { emotionLabels }.flatten().shuffled()


        val startButton=findViewById<Button>(R.id.btn_start)
        startButton.setOnClickListener {
            val intent = Intent(this, MyFace1Activity::class.java)
            intent.putStringArrayListExtra("answer_emotionList", ArrayList(fullList))
            intent.putStringArrayListExtra("my_emotionList", ArrayList())
            startActivity(intent)
        }
    }
}