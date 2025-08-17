package com.example.androidlab.ActivityC

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.view.View

class MyFace3Activity : AppCompatActivity() {

    private lateinit var answer_emotionList: ArrayList<String>
    private lateinit var my_emotionList: ArrayList<String>
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_face_3)


        answer_emotionList = intent.getStringArrayListExtra("answer_emotionList") ?: arrayListOf()
        my_emotionList = intent.getStringArrayListExtra("my_emotionList") ?: arrayListOf()
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val test_num=my_emotionList.size-1
        val correct_or_wrong_text=findViewById<TextView>(R.id.correct_or_wrong_text)
        val reason_text=findViewById<TextView>(R.id.reason_text)
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val fullName = document.getString("nickname") ?: "닉네임 없음"

                    // --- 성 떼고 이름만 추출 ---
                    val name = if (fullName.length > 1) fullName.substring(1) else fullName

                    // --- 받침 여부 확인 함수 ---
                    fun hasJongseong(ch: Char): Boolean {
                        return (ch.code in 0xAC00..0xD7A3) && ((ch.code - 0xAC00) % 28 != 0)
                    }

                    // --- “아/야” ---
                    val lastChar = name.lastOrNull()
                    val nameAhYa = if (lastChar != null && hasJongseong(lastChar)) {
                        "${name}아"
                    } else {
                        "${name}야"
                    }

                    // --- “이/가” ---
                    val nameIGa = if (lastChar != null && hasJongseong(lastChar)) {
                        "${name}이가"
                    } else {
                        "${name}가"
                    }

                    // --- 결과 출력 ---
                    if (answer_emotionList[test_num] == my_emotionList[test_num]) {
                        correct_or_wrong_text.text = "정답입니다!"
                        reason_text.text = "${nameAhYa} 너무 잘했어!"
                    } else {
                        correct_or_wrong_text.text = "틀렸습니다"
                        reason_text.text = "정답: ${answer_emotionList[test_num]}" +
                                "\n${nameIGa} 지은 표정: ${my_emotionList[test_num]}"
                    }
                }
        }


        val startButton=findViewById<Button>(R.id.btn_start)
        startButton.setOnClickListener {
            if(answer_emotionList.size==my_emotionList.size){
                val intent = Intent(this, MyFace4Activity::class.java)
                intent.putStringArrayListExtra("answer_emotionList", answer_emotionList)
                intent.putStringArrayListExtra("my_emotionList", my_emotionList)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this, MyFace1Activity::class.java)
                intent.putStringArrayListExtra("answer_emotionList", answer_emotionList)
                intent.putStringArrayListExtra("my_emotionList", my_emotionList)
                startActivity(intent)
                finish()
            }
        }

    }
}
