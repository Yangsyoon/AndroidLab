package com.example.androidlab.ActivityC

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.R

class MyFace3Activity : AppCompatActivity() {

    private lateinit var answer_emotionList: ArrayList<String>
    private lateinit var my_emotionList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_face_3)

        answer_emotionList = intent.getStringArrayListExtra("answer_emotionList") ?: arrayListOf()
        my_emotionList = intent.getStringArrayListExtra("my_emotionList") ?: arrayListOf()
        val test_num=my_emotionList.size-1
        val correct_or_wrong_text=findViewById<TextView>(R.id.correct_or_wrong_text)
        val reason_text=findViewById<TextView>(R.id.reason_text)
        if(answer_emotionList[test_num]==my_emotionList[test_num]){
            correct_or_wrong_text.setText("정답입니다!")
            reason_text.setText("")
        }else{
            correct_or_wrong_text.setText("틀렸습니다")
            reason_text.setText("정답: "+answer_emotionList[test_num]+
                    "\nOOO님이 지은 표정: "+my_emotionList[test_num])
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
