package com.example.androidlab.ActivityA
// EmotionModeSelectActivity.kt
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.androidlab.BaseActivity
import com.example.androidlab.R

class EmotionModeSelectActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_mode_select)

        val practiceButton: View = findViewById(R.id.button_practice)
        val testButton: View = findViewById(R.id.button_test)

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
