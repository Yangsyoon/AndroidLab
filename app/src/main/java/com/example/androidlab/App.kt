package com.example.androidlab

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.View
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                val green = ContextCompat.getColor(activity, R.color.soft_green)
                activity.window.statusBarColor = green
                activity.window.navigationBarColor = green

                var flags = activity.window.decorView.systemUiVisibility
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
                activity.window.decorView.systemUiVisibility = flags
            }
            // 나머지 콜백은 비워둬도 됨
            override fun onActivityStarted(a: Activity) {}
            override fun onActivityResumed(a: Activity) {}
            override fun onActivityPaused(a: Activity) {}
            override fun onActivityStopped(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(a: Activity) {}
        })
    }
}
