// 이게 있어야 함
package com.example.androidlab.ActivityA

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.androidlab.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


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
    private lateinit var resultContainer: LinearLayout
    private lateinit var resultText: TextView
    private lateinit var checkAnswerButton: View

    private var selectedEmotion: String? = null
    private var isAnswerRevealed = false

    private val totalQuestions = questionList.size

    // ✅ 변경: 감정별 정답/오답 카운트 배열 추가 (0=화남, 1=기쁨, 2=놀람, 3=슬픔)
    private val correctCounts = IntArray(4) { 0 }
    private val wrongCounts = IntArray(4) { 0 }

    // ✅ 변경: Firebase 사용 준비
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private var selectedEmotionView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_quiz)

        imageView = findViewById(R.id.image_question)
        resultIcon = findViewById(R.id.image_result_icon)
        resultContainer = findViewById(R.id.result_container)
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
        val emotions = listOf("화남", "기쁨", "놀람", "슬픔")
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

        resultIcon.setImageResource(if (isCorrect) R.drawable.icon_correct3 else R.drawable.icon_wrong3)
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

        // ✅ 변경: 감정 인덱스 찾기 및 카운트 증가
        val index = when (correct) {
            "화남" -> 0
            "기쁨" -> 1
            "놀람" -> 2
            "슬픔" -> 3
            else -> -1
        }
        if (index != -1) {
            if (isCorrect) correctCounts[index]++ else wrongCounts[index]++
        }

        val message = "정답은 $correct 입니다"
        showResult(isCorrect, message)

        Handler(Looper.getMainLooper()).postDelayed({
            goToNextQuestion()
        }, 3000)
    }

    private fun loadQuestion() {
        val question = questionList[currentIndex]
        imageView.setImageResource(question.imageResId)

        // 결과 컨테이너 완전 초기화
        resultContainer.apply {
            visibility = View.GONE
            alpha = 1f
            animate().cancel()  // 혹시 애니메이션 중단
        }

        resultIcon.apply {
            setImageDrawable(null)
            visibility = View.GONE
            alpha = 1f
            animate().cancel()
        }

        resultText.apply {
            text = ""
            visibility = View.GONE
            alpha = 1f
            animate().cancel()
        }

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
            // ✅ 변경: 마지막 문제 후 Firebase 저장
            saveResultsToFirebase()
            Toast.makeText(this, "퀴즈 완료!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // ✅ 변경: Firebase 저장 함수 추가
    private fun saveResultsToFirebase() {
        lifecycleScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val scoreData = hashMapOf(
                    "userId" to currentUser.uid,
                    "date" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "angryCorrect" to correctCounts[0],
                    "angryWrong" to wrongCounts[0],
                    "happyCorrect" to correctCounts[1],
                    "happyWrong" to wrongCounts[1],
                    "surprisedCorrect" to correctCounts[2],
                    "surprisedWrong" to wrongCounts[2],
                    "sadCorrect" to correctCounts[3],
                    "sadWrong" to wrongCounts[3]
                )

                firestore.collection("testScores")
                    .add(scoreData)
                    .addOnSuccessListener {
                        Log.d("EmotionQuiz", "저장 성공: ${it.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("EmotionQuiz", "저장 실패", e)
                    }
            }
        }
    }
}
