package com.example.androidlab.ActivityA
// EmotionModeSelectActivity.kt
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.R

class EmotionModeSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_mode_select)

        val practiceButton = findViewById<FrameLayout>(R.id.button_practice)
        val testButton = findViewById<FrameLayout>(R.id.button_test)

        practiceButton.setOnClickListener {
            val intent = Intent(this, EmotionPracticeActivity::class.java)
            startActivity(intent)
        }

        testButton.setOnClickListener {
            val intent = Intent(this, EmotionQuizActivity::class.java)
            startActivity(intent)
        }
    }
}
