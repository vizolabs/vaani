package com.vaani.keyboard.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vaani.keyboard.R
import com.vaani.keyboard.util.Prefs
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        prefs = Prefs(this)
        prefs.markActive()

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> getString(R.string.greeting_morning)
            hour < 17 -> getString(R.string.greeting_afternoon)
            else -> getString(R.string.greeting_evening)
        }

        findViewById<TextView>(R.id.tv_greeting).text = greeting
        findViewById<TextView>(R.id.tv_status).text =
            getString(R.string.dashboard_ready, "हिन्दी")
    }
}
