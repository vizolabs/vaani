package com.vaani.keyboard.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.vaani.keyboard.util.Prefs

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        prefs.markActive()
        setupStatusBar()
    }

    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor("#16161F")
            window.navigationBarColor = Color.parseColor("#16161F")
        }
    }

    protected fun setStatusBarLightIcons(light: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val vis = window.decorView.systemUiVisibility
            window.decorView.systemUiVisibility = if (light) {
                vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    protected fun finishWithTransition() {
        finish()
        overridePendingTransition(
            com.vaani.keyboard.R.anim.fade_in,
            com.vaani.keyboard.R.anim.fade_out
        )
    }
}
