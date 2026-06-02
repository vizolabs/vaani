package com.vaani.keyboard.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.vaani.keyboard.R
import com.vaani.keyboard.nav.Navigator

class SetupActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        findViewById<TextView>(R.id.tv_welcome).text =
            getString(R.string.welcome_hindi)

        findViewById<Button>(R.id.btn_finish_setup).setOnClickListener {
            prefs.isSetupComplete = true
            prefs.selectedLanguage = "hi"
            Navigator.toDashboard(this)
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
