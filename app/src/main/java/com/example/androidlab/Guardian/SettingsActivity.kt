package com.example.androidlab.Guardian

import android.content.Context
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.ActivityD.MyFaceScore
import com.example.androidlab.ActivityD.TestScore
import com.example.androidlab.BaseActivity
import com.example.androidlab.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SettingsActivity : BaseActivity() {

    private lateinit var switchSound: Switch
    private lateinit var switchVibration: Switch
    private lateinit var recommand_text: TextView

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var myFaceScoreValue: Int? = null
    private var testScoreValue: Int? = null

    // 불러올 점수 타입 설정: "latest" or "highest"
    private val loadScoreType = "latest" // "latest" 로 바꾸면 최근점수 버전

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        switchVibration = findViewById(R.id.switch_vibration)
        recommand_text = findViewById(R.id.recommand_text)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        switchVibration.isChecked = sharedPref.getBoolean("vibration", true)

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("vibration", isChecked).apply()
        }

        when (loadScoreType) {
            "latest" -> checkLatestScoresAndSetText()
            "highest" -> checkHighestScoresAndSetText()
            else -> checkLatestScoresAndSetText()
        }
    }

    private fun calculateScore(corrects: List<Int>, wrongs: List<Int>): Int {
        val totalCorrect = corrects.sum()
        val totalWrong = wrongs.sum()
        val total = totalCorrect + totalWrong
        return if (total > 0) (totalCorrect.toFloat() / total * 100).toInt() else 0
    }

    private fun checkLatestScoresAndSetText() {
        val uid = auth.currentUser?.uid ?: return

        val faceScoreQuery = db.collection("faceScores")
            .whereEqualTo("userId", uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1)

        val testScoreQuery = db.collection("testScores")
            .whereEqualTo("userId", uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1)

        faceScoreQuery.get().addOnSuccessListener { faceDocs ->
            myFaceScoreValue = if (!faceDocs.isEmpty) {
                val faceScore = faceDocs.documents[0].toObject(MyFaceScore::class.java)
                if (faceScore != null) {
                    calculateScore(
                        listOf(
                            faceScore.angryCorrect,
                            faceScore.happyCorrect,
                            faceScore.neutralCorrect,
                            faceScore.surprisedCorrect,
                            faceScore.sadCorrect
                        ),
                        listOf(
                            faceScore.angryWrong,
                            faceScore.happyWrong,
                            faceScore.neutralWrong,
                            faceScore.surprisedWrong,
                            faceScore.sadWrong
                        )
                    )
                } else 0
            } else 0

            updateRecommendTextIfNeeded()
        }

        testScoreQuery.get().addOnSuccessListener { testDocs ->
            testScoreValue = if (!testDocs.isEmpty) {
                val testScore = testDocs.documents[0].toObject(TestScore::class.java)
                if (testScore != null) {
                    calculateScore(
                        listOf(
                            testScore.angryCorrect,
                            testScore.happyCorrect,
                            testScore.surprisedCorrect,
                            testScore.sadCorrect
                        ),
                        listOf(
                            testScore.angryWrong,
                            testScore.happyWrong,
                            testScore.surprisedWrong,
                            testScore.sadWrong
                        )
                    )
                } else 0
            } else 0

            updateRecommendTextIfNeeded()
        }
    }

    private fun checkHighestScoresAndSetText() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("faceScores")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { faceDocs ->
                var maxFaceScore = 0
                for (doc in faceDocs.documents) {
                    val faceScore = doc.toObject(MyFaceScore::class.java)
                    if (faceScore != null) {
                        val score = calculateScore(
                            listOf(
                                faceScore.angryCorrect,
                                faceScore.happyCorrect,
                                faceScore.neutralCorrect,
                                faceScore.surprisedCorrect,
                                faceScore.sadCorrect
                            ),
                            listOf(
                                faceScore.angryWrong,
                                faceScore.happyWrong,
                                faceScore.neutralWrong,
                                faceScore.surprisedWrong,
                                faceScore.sadWrong
                            )
                        )
                        if (score > maxFaceScore) maxFaceScore = score
                    }
                }
                myFaceScoreValue = maxFaceScore
                updateRecommendTextIfNeeded()
            }

        db.collection("testScores")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { testDocs ->
                var maxTestScore = 0
                for (doc in testDocs.documents) {
                    val testScore = doc.toObject(TestScore::class.java)
                    if (testScore != null) {
                        val score = calculateScore(
                            listOf(
                                testScore.angryCorrect,
                                testScore.happyCorrect,
                                testScore.surprisedCorrect,
                                testScore.sadCorrect
                            ),
                            listOf(
                                testScore.angryWrong,
                                testScore.happyWrong,
                                testScore.surprisedWrong,
                                testScore.sadWrong
                            )
                        )
                        if (score > maxTestScore) maxTestScore = score
                    }
                }
                testScoreValue = maxTestScore
                updateRecommendTextIfNeeded()
            }
    }

    private fun updateRecommendTextIfNeeded() {
        val faceScore = myFaceScoreValue
        val testScore = testScoreValue

        if (faceScore == null || testScore == null) return

        if (faceScore >= 70 && testScore >= 70) {
            recommand_text.text = "자녀의 안정적인 학습 성취(70점 이상)가 확인되었습니다.\n" +
                    "더 이상 보조적인 피드백에 의존하지 않아도\n"+ "원활한 학습이 가능할 것으로 예상되므로,\n"+"진동 피드백 끄시는 것을 권유드립니다"
        }
    }
}

