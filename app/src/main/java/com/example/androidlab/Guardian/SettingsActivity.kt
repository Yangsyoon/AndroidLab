package com.example.androidlab.Guardian

import android.content.Context
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchSound: Switch
    private lateinit var switchVibration: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        switchSound = findViewById(R.id.switch_sound)
        switchVibration = findViewById(R.id.switch_vibration)

        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        switchSound.isChecked = sharedPref.getBoolean("sound", true)
        switchVibration.isChecked = sharedPref.getBoolean("vibration", true)

        switchSound.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("sound", isChecked).apply()
        }

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("vibration", isChecked).apply()
        }
    }
}
