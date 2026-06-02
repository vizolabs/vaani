package com.vaani.keyboard.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.vaani.keyboard.R
import com.vaani.keyboard.util.Prefs

class SplashActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        prefs = Prefs(this)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 1500)
    }

    private fun navigateNext() {
        val intent = if (prefs.isSetupComplete) {
            Intent(this, DashboardActivity::class.java)
        } else {
            Intent(this, SetupActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
