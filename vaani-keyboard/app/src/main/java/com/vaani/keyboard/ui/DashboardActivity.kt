package com.vaani.keyboard.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vaani.keyboard.R
import com.vaani.keyboard.util.Prefs

class DashboardActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        prefs = Prefs(this)

        findViewById<TextView>(R.id.tv_status).text =
            getString(R.string.dashboard_ready, "हिन्दी")
    }
}
