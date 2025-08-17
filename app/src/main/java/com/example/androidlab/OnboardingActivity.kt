package com.example.androidlab

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnStart: Button

    private val images = listOf(R.drawable.main_screen, R.drawable.a_1, R.drawable.a_2)
    private val titles = listOf("첫 번째 화면", "두 번째 화면", "세 번째 화면")
    private val descriptions = listOf("앱 소개 1", "앱 소개 2", "앱 소개 3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        btnStart = findViewById(R.id.btnStart)

        tvTitle.text = titles[0]
        tvDescription.text = descriptions[0]
        btnStart.visibility = View.GONE

        viewPager.adapter = OnboardingAdapter(images)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tvTitle.text = titles[position]
                tvDescription.text = descriptions[position]
                btnStart.visibility = if (position == images.size - 1) View.VISIBLE else View.GONE
            }
        })

        btnStart.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
    }
}
