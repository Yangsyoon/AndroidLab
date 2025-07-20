package com.example.androidlab.ActivityD

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.example.androidlab.R
import com.example.androidlab.database.MyFaceScore
import com.example.androidlab.database.MyFaceScoreDAO
import com.example.androidlab.database.MyFaceScoreDatabase
import com.example.androidlab.database.TestScore
import com.example.androidlab.database.TestScoreDAO
import com.example.androidlab.database.TestScoreDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class MyRecordActivity : AppCompatActivity() {

    private lateinit var myFaceScoreDao: MyFaceScoreDAO
    private lateinit var testScoreDao: TestScoreDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_record)

        myFaceScoreDao = MyFaceScoreDatabase.getInstance(applicationContext).myFaceScoreDAO()
        testScoreDao = TestScoreDatabase.getInstance(applicationContext).testScoreDAO()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                checkAndInsertDummyData()
            }
        }

        val goToTestScoreButton = findViewById<ConstraintLayout>(R.id.btn_goToTestScore)
        val goToMyFaceScoreButton = findViewById<ConstraintLayout>(R.id.btn_goToMyFaceScore)

        goToMyFaceScoreButton.setOnClickListener {
            val intent = Intent(this, MyFaceScoreActivity::class.java)
            startActivity(intent)
        }
        goToTestScoreButton.setOnClickListener {
            val intent = Intent(this, TestScoreActivity::class.java)
            startActivity(intent)
        }
    }

    private suspend fun checkAndInsertDummyData() {
        val faceScoreCount = myFaceScoreDao.getCount()
        val testScoreCount = testScoreDao.getCount()

        if (testScoreCount == 0) {
            val dummyTestScores = generateDummyTestScores()
            testScoreDao.insertAll(dummyTestScores)
        }
    }


    private fun generateDummyTestScores(): List<TestScore> {
        val now = LocalDateTime.now()
        return List(9) { i ->
            TestScore(
                date = now.minusDays(i.toLong()),
                emotion1Correct = 60-i,
                emotion1Wrong = 40+i,
                emotion2Correct = 70-i,
                emotion2Wrong = 30+i,
                emotion3Correct = 50-i,
                emotion3Wrong = 50+i,
                emotion4Correct = 80-i,
                emotion4Wrong = 20+i
            )
        }
    }
}
