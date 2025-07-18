package com.example.androidlab
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EmotionPracticeActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var answerText: TextView
    private lateinit var nextButton: Button
    private lateinit var finishPracticeButton: Button

    private val emotionImages = listOf(
        R.drawable.train_happy_sample1 to "기쁨",
        R.drawable.train_sad_sample1 to "슬픔",
        R.drawable.train_angry_sample1 to "화남",
        R.drawable.train_surprised_sample1 to "놀람",

    )

    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_practice)

        imageView = findViewById(R.id.image_emotion)
        answerText = findViewById(R.id.text_emotion_answer)
        nextButton = findViewById(R.id.btn_next_image)

        showCurrentEmotion()

        nextButton.setOnClickListener {
            currentIndex++
            if (currentIndex >= emotionImages.size) {
                Toast.makeText(this, "모든 이미지를 확인했어요!", Toast.LENGTH_SHORT).show()
                currentIndex = 0 // 또는 finish() 등
                onPracticeComplete()
            }
            showCurrentEmotion()
        }
        finishPracticeButton = findViewById(R.id.button_finish_practice)
        finishPracticeButton.setOnClickListener {
            // 연습 완료 버튼 눌렀을 때 처리
            val intent = Intent(this, EmotionModeSelectActivity::class.java)
            // 기존 스택을 초기화하고 새로 시작하려면 아래 플래그 추가 가능
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }
    }
    // 감정 연습이 모두 끝났을 때도 같은 방식으로 돌아가기
    private fun onPracticeComplete() {
        val intent = Intent(this, EmotionModeSelectActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }


    private fun showCurrentEmotion() {
        val (imageRes, answer) = emotionImages[currentIndex]
        imageView.setImageResource(imageRes)
        answerText.text = "감정: $answer"
    }
}
