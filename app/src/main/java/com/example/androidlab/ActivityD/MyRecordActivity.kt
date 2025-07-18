package com.example.androidlab.ActivityD

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.R

class MyRecordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_record)

        val goToTestScoreButton=findViewById<Button>(R.id.btn_goToTestScore);
        val goToMyFaceScoreButton=findViewById<Button>(R.id.btn_goToMyFaceScore);


    }

}