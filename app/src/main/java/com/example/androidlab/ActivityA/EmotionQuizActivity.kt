// 이게 있어야 함
package com.example.androidlab.ActivityA

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.R


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
    private lateinit var resultIcon: ImageView
    private lateinit var resultText: TextView
    private lateinit var checkAnswerButton: Button

    private var selectedEmotion: String? = null
    private var isAnswerRevealed = false

    private val totalQuestions = questionList.size

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_quiz)

        imageView = findViewById(R.id.image_question)
        resultIcon = findViewById(R.id.image_result_icon)
        resultText = findViewById(R.id.text_result_message)
        checkAnswerButton = findViewById(R.id.btn_check_answer)

        updateQuestionNumber()
        loadQuestion()

        checkAnswerButton.setOnClickListener {
            if (selectedEmotion == null) {
                Toast.makeText(this, "감정을 먼저 선택하세요", Toast.LENGTH_SHORT).show()
            } else {
                checkAnswer(selectedEmotion!!)
            }
        }
    }
    private fun setEmotionButtonsEnabled(enabled: Boolean) {
        val emotions = listOf("기쁨", "슬픔", "화남", "놀람")
        for (tag in emotions) {
            val button = findViewByTagInGridLayout(tag)
            button?.isEnabled = enabled
        }
    }

    private fun findViewByTagInGridLayout(tag: String): LinearLayout? {
        val gridLayout = findViewById<GridLayout>(R.id.grid_emotion_buttons) // 실제 ID로 바꾸세요
        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i)
            if (child.tag == tag) {
                return child as? LinearLayout
            }
        }
        return null
    }



    private fun showResult(isCorrect: Boolean, message: String) {
        setEmotionButtonsEnabled(false)
        val resultContainer = findViewById<LinearLayout>(R.id.result_container)
        val resultIcon = findViewById<ImageView>(R.id.image_result_icon)
        val resultMessage = findViewById<TextView>(R.id.text_result_message)

        resultIcon.setImageResource(if (isCorrect) R.drawable.icon_correct else R.drawable.icon_wrong)
        resultMessage.text = message

        resultContainer.visibility = View.VISIBLE
        resultIcon.visibility = View.VISIBLE
        resultText.visibility = View.VISIBLE

    }

    // 문제 번호 갱신 함수
    private fun updateQuestionNumber() {
        val textView = findViewById<TextView>(R.id.text_question_number)
        val formattedText = getString(R.string.question_number, currentIndex + 1, totalQuestions)
        textView.text = formattedText
    }

    private var selectedEmotionView: View? = null

    fun onEmotionClicked(view: View) {
        // 이전에 선택된 버튼이 있다면 배경 복원
        selectedEmotionView?.setBackgroundResource(R.drawable.button_bg)

        // 새로 선택된 버튼은 초록 테두리 적용
        view.setBackgroundResource(R.drawable.selected_border)

        // 선택된 버튼을 저장
        selectedEmotionView = view

        selectedEmotion = view.tag?.toString()

    }


    private fun checkAnswer(selectedEmotion: String) {
        val correct = questionList[currentIndex].correctEmotion
        isAnswerRevealed = true

        val isCorrect = selectedEmotion == correct
        val message = "정답은 $correct 입니다"
        showResult(isCorrect, message)

        // 3초 후 다음 문제로 자동 이동
        Handler(Looper.getMainLooper()).postDelayed({
            goToNextQuestion()
        }, 3000)
    }

    private fun loadQuestion() {
        val question = questionList[currentIndex]
        imageView.setImageResource(question.imageResId)
        resultIcon.visibility = View.GONE
        resultText.visibility = View.GONE
        selectedEmotion = null
        isAnswerRevealed = false
    }


    private fun goToNextQuestion() {
        currentIndex++
        if (currentIndex < questionList.size) {
            selectedEmotionView?.setBackgroundResource(R.drawable.button_bg)
            selectedEmotionView = null
            loadQuestion()
            setEmotionButtonsEnabled(true)
            updateQuestionNumber()
        } else {
            Toast.makeText(this, "퀴즈 완료!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
