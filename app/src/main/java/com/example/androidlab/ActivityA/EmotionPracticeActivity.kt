package com.example.androidlab.ActivityA
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.R
import com.example.androidlab.utils.HapticPrefs
import com.example.androidlab.utils.Haptics

class EmotionPracticeActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var answerText: TextView
    private lateinit var emotionIcon: ImageView      // ← 추가
    private lateinit var nextButton: View
    private lateinit var finishPracticeButton: View

    private val emotionImages = listOf(
        R.drawable.happy_0 to "기쁨",
        R.drawable.sad_0 to "슬픔",
        R.drawable.angry_0 to "화남",
        R.drawable.surprised_0 to "놀람",
    )

    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_practice)

        imageView = findViewById(R.id.image_emotion)
        answerText = findViewById(R.id.text_emotion_answer)
        emotionIcon = findViewById(R.id.image_emotion_icon)   // ← 추가 (XML에 이 ID가 있어야 함)
        nextButton = findViewById(R.id.button_next_practice)
        finishPracticeButton = findViewById(R.id.button_finish_practice)

        showCurrentEmotion()

        nextButton.setOnClickListener {
            currentIndex++
            if (currentIndex >= emotionImages.size) {
                Toast.makeText(this, "모든 이미지를 확인했어요!", Toast.LENGTH_SHORT).show()
                onPracticeComplete()
                return@setOnClickListener
            }
            showCurrentEmotion()
        }

        finishPracticeButton.setOnClickListener {
            val intent = Intent(this, EmotionModeSelectActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun onPracticeComplete() {
        val intent = Intent(this, EmotionModeSelectActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun showCurrentEmotion() {
        val (imageRes, answer) = emotionImages[currentIndex]
        imageView.setImageResource(imageRes)
        answerText.text = "감정: $answer"

        // 감정 문자열에 따른 아이콘 매핑
        val iconRes = when (answer) {
            "기쁨" -> R.drawable.happy_icon2
            "슬픔" -> R.drawable.sad_icon2
            "화남" -> R.drawable.angry_icon2
            "놀람" -> R.drawable.surprised_icon2
            else   -> R.drawable.happy_icon2
        }
        emotionIcon.setImageResource(iconRes)
        emotionIcon.contentDescription = "감정 아이콘: $answer"
        // ← 추가: 감정에 따른 진동 피드백
        Haptics.vibrateEmotion(this, answer)
    }
}

