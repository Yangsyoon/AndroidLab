package com.example.androidlab.ActivityC

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.androidlab.MainActivity
import com.example.androidlab.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

class MyFace4Activity : AppCompatActivity() {

    private lateinit var answer_emotionList: ArrayList<String>
    private lateinit var my_emotionList: ArrayList<String>

    private val emotionLabels = listOf("화남", "기쁨", "무표정", "놀람", "슬픔")

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_face_4)

        answer_emotionList = intent.getStringArrayListExtra("answer_emotionList") ?: arrayListOf()
        my_emotionList = intent.getStringArrayListExtra("my_emotionList") ?: arrayListOf()

        val tvResult = findViewById<TextView>(R.id.tvResult)

        // 정답 개수 계산
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

        // Firestore에 저장
        lifecycleScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val scoreData = hashMapOf(
                    "userId" to currentUser.uid,
                    "date" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "angryCorrect" to correctCounts[0],
                    "angryWrong" to wrongCounts[0],
                    "happyCorrect" to correctCounts[1],
                    "happyWrong" to wrongCounts[1],
                    "neutralCorrect" to correctCounts[2],
                    "neutralWrong" to wrongCounts[2],
                    "surprisedCorrect" to correctCounts[3],
                    "surprisedWrong" to wrongCounts[3],
                    "sadCorrect" to correctCounts[4],
                    "sadWrong" to wrongCounts[4]
                )

                firestore.collection("faceScores")
                    .add(scoreData)
                    .addOnSuccessListener {
                        // 저장 성공 시 로그나 토스트 추가 가능
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
        }

        val home_btn = findViewById<Button>(R.id.home_btn)
        home_btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
