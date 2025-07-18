package com.example.androidlab.ActivityD

import android.os.Bundle
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.R

class TestScoreActivity: AppCompatActivity() {

    private lateinit var toggleGraph: ToggleButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_score)

        toggleGraph = findViewById(R.id.toggleGraph)
        toggleGraph.setChecked(false)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TestScoreListFragment())
                .commit()
        }

        toggleGraph.setOnCheckedChangeListener { _, isChecked ->
            val fragment = if (isChecked) {
                TestScoreGraphFragment()
            } else {
                TestScoreListFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }
}