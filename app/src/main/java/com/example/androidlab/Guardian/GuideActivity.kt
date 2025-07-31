package com.example.androidlab.Guardian

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.databinding.ActivityGuideBinding

class GuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "앱 사용법 안내"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 예: 확인 버튼 누르면 종료
        binding.btnConfirm.setOnClickListener {
            finish()
        }
    }

    // 뒤로가기 버튼 기능 활성화
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
