package com.example.androidlab.ActivityD

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.R

class MyFaceScoreActivity: AppCompatActivity() {

    private lateinit var toggleGraph: ToggleButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_face_score)

        val backButton = findViewById<ImageButton>(R.id.buttonBack)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        toggleGraph = findViewById(R.id.toggleGraph)
        toggleGraph.setChecked(false)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MyFaceScoreListFragment())
                .commit()
        }

        toggleGraph.setOnCheckedChangeListener { _, isChecked ->
            val fragment = if (isChecked) {
                MyFaceScoreGraphFragment()
            } else {
                MyFaceScoreListFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }
}