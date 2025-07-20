package com.example.androidlab.ActivityC

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.androidlab.MainActivity
import com.example.androidlab.R
import com.example.androidlab.database.MyFaceScore
import com.example.androidlab.database.MyFaceScoreDatabase
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MyFace4Activity : AppCompatActivity() {

    private lateinit var answer_emotionList: ArrayList<String>
    private lateinit var my_emotionList: ArrayList<String>

    private val emotionLabels = listOf("분노", "혐오", "두려움", "기쁨", "슬픔", "놀람", "무표정")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_face_4)

        answer_emotionList = intent.getStringArrayListExtra("answer_emotionList") ?: arrayListOf()
        my_emotionList = intent.getStringArrayListExtra("my_emotionList") ?: arrayListOf()

        val tvResult = findViewById<TextView>(R.id.tvResult)

        // 서로 같은 위치에서 값이 일치하는 항목 수 계산
        val correctCount = answer_emotionList.zip(my_emotionList).count { it.first == it.second }

        tvResult.text = "일치한 감정 개수: $correctCount / ${answer_emotionList.size}"

        val correctCounts = IntArray(emotionLabels.size)
        val wrongCounts = IntArray(emotionLabels.size)

        for (i in answer_emotionList.indices) {
            val answer = answer_emotionList[i]
            val myAnswer = my_emotionList[i]
            val index = emotionLabels.indexOf(answer)
            if (index != -1) {
                if (answer == myAnswer) {
                    correctCounts[index] += 1
                } else {
                    wrongCounts[index] += 1
                }
            }
        }


        lifecycleScope.launch {
            val dao = MyFaceScoreDatabase.getInstance(applicationContext).myFaceScoreDAO()

            val score = MyFaceScore(
                date = LocalDateTime.now(),
                emotion1Correct = correctCounts[0],
                emotion1Wrong = wrongCounts[0],
                emotion2Correct = correctCounts[1],
                emotion2Wrong = wrongCounts[1],
                emotion3Correct = correctCounts[2],
                emotion3Wrong = wrongCounts[2],
                emotion4Correct = correctCounts[3],
                emotion4Wrong = wrongCounts[3],
                emotion5Correct = correctCounts[4],
                emotion5Wrong = wrongCounts[4],
                emotion6Correct = correctCounts[5],
                emotion6Wrong = wrongCounts[5],
                emotion7Correct = correctCounts[6],
                emotion7Wrong = wrongCounts[6]
            )

            dao.insert(score)
        }


        val home_btn=findViewById<Button>(R.id.home_btn)
        home_btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
