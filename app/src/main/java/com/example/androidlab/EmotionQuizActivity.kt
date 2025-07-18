// 이게 있어야 함
package com.example.androidlab

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class EmotionQuizActivity : AppCompatActivity() {

    data class Question(val imageResId: Int, val correctEmotion: String)

    private val questionList = listOf(
        Question(R.drawable.test_surprised_sample1, "놀람"),
        Question(R.drawable.test_sad_sample1, "슬픔"),
        Question(R.drawable.test_angry_sample1, "화남"),
        Question(R.drawable.test_happy_sample1, "기쁨"),
        // 최대 16문제까지 추가 가능
    )

    private var currentIndex = 0
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_quiz)

        imageView = findViewById(R.id.image_question)

        loadQuestion()

        // 다음 버튼 클릭 처리
        findViewById<View>(R.id.btn_next).setOnClickListener {
            goToNextQuestion()
        }
    }

    /** 공통 클릭 이벤트: 감정 버튼에 설정된 tag 값을 가져와 정답 확인 */
    fun onEmotionClicked(view: View) {
        val selectedEmotion = view.tag?.toString()
        if (selectedEmotion != null) {
            checkAnswer(selectedEmotion)
        }
    }

    private fun checkAnswer(selectedEmotion: String) {
        val correct = questionList[currentIndex].correctEmotion
        if (selectedEmotion == correct) {
            Toast.makeText(this, "정답입니다!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "틀렸습니다. 정답은 $correct", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadQuestion() {
        imageView.setImageResource(questionList[currentIndex].imageResId)
    }

    private fun goToNextQuestion() {
        currentIndex++
        if (currentIndex < questionList.size) {
            loadQuestion()
        } else {
            Toast.makeText(this, "퀴즈 완료!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
