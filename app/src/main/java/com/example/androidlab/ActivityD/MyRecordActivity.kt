package com.example.androidlab.ActivityD

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.androidlab.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class MyRecordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_record)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val goToTestScoreButton = findViewById<ConstraintLayout>(R.id.btn_goToTestScore)
        val goToMyFaceScoreButton = findViewById<ConstraintLayout>(R.id.btn_goToMyFaceScore)

        val nicknameTextView = findViewById<TextView>(R.id.score_board_text)
        val testScoreText = findViewById<TextView>(R.id.detective_score_board_text)
        val myFaceScoreText = findViewById<TextView>(R.id.facial_score_board_text)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val fullName = document.getString("nickname") ?: "닉네임 없음"

                    // ✅ 성 빼고 이름만 사용
                    val name = if (fullName.length > 1) fullName.substring(1) else fullName

                    // 마지막 글자 추출
                    val lastChar = name.lastOrNull()
                    val possessive = if (lastChar != null && lastChar.code in 0xAC00..0xD7A3) {
                        val hasJongseong = (lastChar.code - 0xAC00) % 28 != 0
                        if (hasJongseong) "${name}이의" else "${name}의"
                    } else {
                        "${name}의" // 한글이 아닐 경우 기본 "의"
                    }

                    nicknameTextView.text = "${possessive}\n최근점수야!"
                }
        }

        // 최근 testscore 가져오기
        if (uid != null) {
            db.collection("testScores")
                .whereEqualTo("userId", uid)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val latest = documents.documents[0].toObject(TestScore::class.java)
                        if (latest != null) {
                            val score = calculateScore(
                                listOf(
                                    latest.angryCorrect,
                                    latest.happyCorrect,
                                    latest.surprisedCorrect,
                                    latest.sadCorrect
                                ),
                                listOf(
                                    latest.angryWrong,
                                    latest.happyWrong,
                                    latest.surprisedWrong,
                                    latest.sadWrong
                                )
                            )
                            testScoreText.text = "${score}점"
                        }
                    }
                }
        }

        // 최근 myfacescore 가져오기
        if (uid != null) {
            db.collection("faceScores")
                .whereEqualTo("userId", uid)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val latest = documents.documents[0].toObject(MyFaceScore::class.java)
                        if (latest != null) {
                            val score = calculateScore(
                                listOf(
                                    latest.angryCorrect,
                                    latest.happyCorrect,
                                    latest.neutralCorrect,
                                    latest.surprisedCorrect,
                                    latest.sadCorrect
                                ),
                                listOf(
                                    latest.angryWrong,
                                    latest.happyWrong,
                                    latest.neutralWrong,
                                    latest.surprisedWrong,
                                    latest.sadWrong
                                )
                            )
                            myFaceScoreText.text = "${score}점"
                        }
                    }
                }
        }

        // 버튼 이벤트
        goToMyFaceScoreButton.setOnClickListener {
            startActivity(Intent(this, MyFaceScoreActivity::class.java))
        }
        goToTestScoreButton.setOnClickListener {
            startActivity(Intent(this, TestScoreActivity::class.java))
        }
    }

    private fun calculateScore(corrects: List<Int>, wrongs: List<Int>): Int {
        val totalCorrect = corrects.sum()
        val totalWrong = wrongs.sum()
        val total = totalCorrect + totalWrong
        return if (total > 0) {
            (totalCorrect.toFloat() / total * 100).toInt()
        } else {
            0
        }
    }
}