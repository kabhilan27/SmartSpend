package com.example.smartspend

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide status bar and action bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.hide(android.view.WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        setContentView(R.layout.activity_home)

        // Navigate to landing1 after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, landing1::class.java)
            startActivity(intent)
            finish() // Close home activity after navigation
        }, 2000) // 2000 milliseconds = 2 seconds
    }
}