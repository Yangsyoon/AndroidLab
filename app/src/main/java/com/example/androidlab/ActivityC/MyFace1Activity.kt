package com.example.androidlab.ActivityC

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.R
import com.example.androidlab.utils.Haptics
class MyFace1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_face_1)


        val answer_emotionList = intent.getStringArrayListExtra("answer_emotionList") ?: arrayListOf()
        val my_emotionList = intent.getStringArrayListExtra("my_emotionList") ?: arrayListOf()
        val test_num=my_emotionList.size

        val face_text=findViewById<TextView>(R.id.face_text)
        val answerEmotion = answer_emotionList[test_num] // 정답 감정
        face_text.text = answerEmotion

        // 🔹 정답 감정에 따라 진동 실행
        Haptics.vibrateEmotion(this, answerEmotion)

        val startButton=findViewById<Button>(R.id.btn_start)
        startButton.setOnClickListener {
            val intent = Intent(this, MyFace2Activity::class.java)
            intent.putStringArrayListExtra("answer_emotionList", answer_emotionList)
            intent.putStringArrayListExtra("my_emotionList", my_emotionList)
            startActivity(intent)
            finish()
        }

    }
}