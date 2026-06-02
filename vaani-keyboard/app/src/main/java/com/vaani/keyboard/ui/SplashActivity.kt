package com.vaani.keyboard.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.vaani.keyboard.R
import com.vaani.keyboard.nav.Navigator

class SplashActivity : BaseActivity() {

    private var navigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        findViewById<View>(R.id.splash_logo).alpha = 0f
        findViewById<View>(R.id.splash_logo).animate()
            .alpha(1f)
            .setDuration(800)
            .start()

        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 1500)
    }

    private fun navigateNext() {
        if (navigated) return
        navigated = true
        if (prefs.isSetupComplete) {
            Navigator.toDashboard(this, finishCurrent = false)
        } else {
            Navigator.toSetup(this, finishCurrent = false)
        }
        finishWithTransition()
    }
}
