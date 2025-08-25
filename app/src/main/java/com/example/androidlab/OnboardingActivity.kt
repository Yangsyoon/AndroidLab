package com.example.androidlab

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : BaseActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btnStart: Button

    private val images = listOf(R.drawable.a_2, R.drawable.a_3, R.drawable.b_1, R.drawable.c_2, R.drawable.d_3, R.drawable.e_2, R.drawable.e_3, R.drawable.e_4)
    private val titles = listOf(R.string.title1, R.string.title1,R.string.title2,R.string.title3,R.string.title4,R.string.title5,R.string.title6,R.string.title7)
    private val descriptions = listOf(R.string.paragraph1_1,R.string.paragraph1_2,R.string.paragraph2_1,R.string.paragraph3_1,R.string.paragraph4_1,R.string.paragraph5_1,R.string.paragraph6_1,R.string.paragraph7_1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        btnStart = findViewById(R.id.btnStart)

        btnStart.visibility = View.GONE

        viewPager.adapter = OnboardingAdapter(images,titles,descriptions)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
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
