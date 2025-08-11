package com.example.androidlab

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.androidlab.ActivityA.EmotionModeSelectActivity
import com.example.androidlab.ActivityB.YourFace1Activity
import com.example.androidlab.ActivityC.MyFaceActivity
import com.example.androidlab.ActivityD.MyRecordActivity
import com.example.androidlab.Guardian.GuideActivity
import com.example.androidlab.Guardian.SettingsActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

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
                    startActivity(Intent(this, GuideActivity::class.java))
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
