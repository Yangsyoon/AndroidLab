package com.example.androidlab.ActivityD

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.androidlab.R
import kotlin.jvm.java

class MyRecordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_record)

        val goToTestScoreButton=findViewById<ConstraintLayout>(R.id.btn_goToTestScore);
        val goToMyFaceScoreButton=findViewById<ConstraintLayout>(R.id.btn_goToMyFaceScore);

        goToMyFaceScoreButton.setOnClickListener {
            val intent = Intent(this, MyFaceScoreActivity::class.java)
            startActivity(intent)
        }
        goToTestScoreButton.setOnClickListener {
            val intent = Intent(this, TestScoreActivity::class.java)
            startActivity(intent)
        }
    }

}