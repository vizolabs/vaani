package com.vaani.keyboard.nav

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.vaani.keyboard.ui.DashboardActivity
import com.vaani.keyboard.ui.SettingsActivity
import com.vaani.keyboard.ui.SetupActivity
import com.vaani.keyboard.ui.SplashActivity

object Navigator {

    fun toSplash(from: Context, finishCurrent: Boolean = true) {
        start(from, SplashActivity::class.java, finishCurrent)
    }

    fun toSetup(from: Context, finishCurrent: Boolean = true) {
        start(from, SetupActivity::class.java, finishCurrent)
    }

    fun toDashboard(from: Context, finishCurrent: Boolean = true) {
        start(from, DashboardActivity::class.java, finishCurrent)
    }

    fun toSettings(from: Context, finishCurrent: Boolean = true) {
        start(from, SettingsActivity::class.java, finishCurrent)
    }

    fun restartToSplash(from: Context) {
        val intent = Intent(from, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        from.startActivity(intent)
        if (from is Activity) {
            from.overridePendingTransition(
                com.vaani.keyboard.R.anim.fade_in,
                com.vaani.keyboard.R.anim.fade_out
            )
            from.finish()
        }
    }

    private fun start(from: Context, target: Class<*>, finishCurrent: Boolean) {
        val intent = Intent(from, target)
        from.startActivity(intent)
        if (from is Activity) {
            from.overridePendingTransition(
                com.vaani.keyboard.R.anim.fade_in,
                com.vaani.keyboard.R.anim.fade_out
            )
            if (finishCurrent) from.finish()
        }
    }
}
