package com.vaani.keyboard.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vaani.keyboard.R
import com.vaani.keyboard.util.Prefs

class SetupActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        prefs = Prefs(this)

        findViewById<Button>(R.id.btn_finish_setup).setOnClickListener {
            prefs.isSetupComplete = true
            prefs.selectedLanguage = "hi"
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}
