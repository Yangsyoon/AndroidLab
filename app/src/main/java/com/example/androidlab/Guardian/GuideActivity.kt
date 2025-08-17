package com.example.androidlab.Guardian

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import com.example.androidlab.MainActivity
import com.example.androidlab.databinding.ActivityGuideBinding

class GuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "앱 사용법 안내"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val videoUri = Uri.parse("android.resource://${packageName}/raw/guide_video")
        binding.videoView.setVideoURI(videoUri)
        binding.videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            binding.videoView.start()
        }


        binding.btnPlayVideo.setOnClickListener {
            // 콘텐츠 영역 숨기고 영상 보이기
            binding.scrollView.visibility = View.GONE
            binding.videoView.visibility = View.VISIBLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = window.insetsController
                controller?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                supportActionBar?.hide()
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                supportActionBar?.hide()
            }

            binding.videoView.setVideoURI(Uri.parse("android.resource://${packageName}/raw/guide_video"))
            binding.videoView.setOnPreparedListener { it.isLooping = true }
            binding.videoView.start()
        }





    }

}
