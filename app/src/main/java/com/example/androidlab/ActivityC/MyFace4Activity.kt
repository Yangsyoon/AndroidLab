package com.example.androidlab.ActivityC

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.R

class MyFace4Activity : AppCompatActivity() {

    private lateinit var answer_emotionList: ArrayList<String>
    private lateinit var my_emotionList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_face_4)

        answer_emotionList = intent.getStringArrayListExtra("answer_emotionList") ?: arrayListOf()
        my_emotionList = intent.getStringArrayListExtra("my_emotionList") ?: arrayListOf()

        val tvResult = findViewById<TextView>(R.id.tvResult)

        // 서로 같은 위치에서 값이 일치하는 항목 수 계산
        val correctCount = answer_emotionList.zip(my_emotionList).count { it.first == it.second }

        tvResult.text = "일치한 감정 개수: $correctCount / ${answer_emotionList.size}"
    }
}
