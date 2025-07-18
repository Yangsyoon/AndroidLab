package com.example.androidlab

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.ActivityD.MyRecordActivity
import com.example.androidlab.EmotionQuizActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnA = findViewById<Button>(R.id.button_detective)
        val btnB = findViewById<Button>(R.id.btnB)
        val btnC = findViewById<Button>(R.id.btnC)
        val btnD = findViewById<Button>(R.id.btnD)

        btnA.setOnClickListener {
            startActivity(Intent(this, EmotionModeSelectActivity::class.java))
        }

        btnB.setOnClickListener {
            startActivity(Intent(this, ActivityB::class.java))
        }

        btnC.setOnClickListener {
            startActivity(Intent(this, ActivityC::class.java))
        }

        btnD.setOnClickListener {
            startActivity(Intent(this, MyRecordActivity::class.java))
        }
    }
}
