package com.example.androidlab.ActivityD

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.example.androidlab.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MyRecordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_record)

        val goToTestScoreButton = findViewById<ConstraintLayout>(R.id.btn_goToTestScore)
        val goToMyFaceScoreButton = findViewById<ConstraintLayout>(R.id.btn_goToMyFaceScore)

        goToMyFaceScoreButton.setOnClickListener {
            startActivity(Intent(this, MyFaceScoreActivity::class.java))
        }
        goToTestScoreButton.setOnClickListener {
            startActivity(Intent(this, TestScoreActivity::class.java))
        }
    }




}
