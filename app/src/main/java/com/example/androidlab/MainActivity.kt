package com.example.androidlab

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.androidlab.ActivityA.EmotionModeSelectActivity
import com.example.androidlab.ActivityB.YourFace1Activity
import com.example.androidlab.ActivityC.MyFaceActivity
import com.example.androidlab.ActivityD.MyRecordActivity
import com.example.androidlab.Guardian.GuideActivity
import com.example.androidlab.Guardian.HapticEditorActivity
import com.example.androidlab.Guardian.HapticOverviewActivity
import com.example.androidlab.Guardian.SettingsActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java
import android.view.View
import androidx.core.app.ActivityCompat


class MainActivity : BaseActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var welcome_text: TextView

    var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        } else {

        }

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        welcome_text=findViewById<TextView>(R.id.welcome_text)
        // 🔹 Toolbar 세팅
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // 전역 테마 대신 코드에서 한 번 더 보정 (통일감)
        toolbar.setBackgroundResource(R.color.soft_green)
        // 필요 시: 타이틀 색
        // toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))

        // 🔹 시스템 바 색 강제 적용 (위/아래 하얀 띠 제거)
        window.statusBarColor = ContextCompat.getColor(this, R.color.soft_green)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.soft_green)

        // 밝은 배경이면 아이콘을 어둡게
        var flags = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        window.decorView.systemUiVisibility = flags

        // 닉네임 가져오기
        val uid = mAuth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val fullName = document.getString("nickname") ?: "닉네임 없음"

                    // 성 떼고 이름만
                    val name = if (fullName.length > 1) fullName.substring(1) else fullName

                    // 받침 여부 판별
                    fun hasJongseong(ch: Char): Boolean {
                        return (ch.code in 0xAC00..0xD7A3) && ((ch.code - 0xAC00) % 28 != 0)
                    }

                    // 아/야 붙이기
                    val lastChar = name.lastOrNull()
                    val nameAhYa = if (lastChar != null && hasJongseong(lastChar)) {
                        "${name}아"
                    } else {
                        "${name}야"
                    }

                    welcome_text.text = "${nameAhYa}!\n만나서 반가워!\n오늘도 같이 놀자!"
                }
        }



        val btnA = findViewById<Button>(R.id.button_detective)
        val btnB = findViewById<Button>(R.id.btnB)
        val btnC = findViewById<Button>(R.id.btnC)
        val btnD = findViewById<Button>(R.id.btnD)

        btnA.setOnClickListener {
            startActivity(Intent(this, EmotionModeSelectActivity::class.java))
        }

        btnB.setOnClickListener {
            startActivity(Intent(this, YourFace1Activity::class.java))
        }

        btnC.setOnClickListener {
            startActivity(Intent(this, MyFaceActivity::class.java))
        }

        btnD.setOnClickListener {
            startActivity(Intent(this, MyRecordActivity::class.java))
        }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_drawer -> {
                    drawerLayout.openDrawer(GravityCompat.END) // 오른쪽에서 drawer 열기
                    true
                }
                else -> false
            }
        }

        // 네비게이션 뷰 메뉴 클릭
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_guide -> {
                    /*startActivity(Intent(this, GuideActivity::class.java))*/
                    startActivity(Intent(this, OnboardingActivity::class.java))
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    drawerLayout.closeDrawers()
                    true
                }

                R.id.btn_logout -> {
                    logout()
                    true
                }
                R.id.haptic_settings -> {
                    startActivity(Intent(this, HapticEditorActivity::class.java))
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.haptic_overview -> {
                    startActivity(Intent(this, HapticOverviewActivity::class.java))
                    drawerLayout.closeDrawers()

                    true
                }

                else -> false
            }
        }
    }

    // 툴바에 메뉴 아이콘 넣기 (오른쪽)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)  // toolbar_menu.xml에서 메뉴 정의
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_drawer -> {
                if (drawerLayout.isDrawerOpen(navView)) {
                    drawerLayout.closeDrawer(navView)
                } else {
                    drawerLayout.openDrawer(navView)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    private fun logout() {
        mAuth.signOut() // Firebase 인증 로그아웃

        // 로그인 화면으로 이동
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish() // 현재 액티비티 종료
    }


}
