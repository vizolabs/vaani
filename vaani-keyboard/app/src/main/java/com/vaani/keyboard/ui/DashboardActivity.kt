package com.vaani.keyboard.ui

import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.vaani.keyboard.R
import com.vaani.keyboard.nav.Navigator
import com.vaani.keyboard.util.PermissionHelper
import java.util.Calendar

class DashboardActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> getString(R.string.greeting_morning)
            hour < 17 -> getString(R.string.greeting_afternoon)
            else -> getString(R.string.greeting_evening)
        }

        val langLabel = when (prefs.selectedLanguage) {
            "hi" -> getString(R.string.subtype_hindi)
            "mr" -> getString(R.string.subtype_marathi)
            else -> getString(R.string.subtype_hinglish)
        }

        findViewById<TextView>(R.id.tv_greeting).text = greeting
        findViewById<TextView>(R.id.tv_status).text =
            getString(R.string.dashboard_ready, langLabel)

        val micStatus = findViewById<TextView>(R.id.tv_mic_status)
        updateMicStatus(micStatus)
        micStatus.setOnClickListener {
            requestMicrophonePermission()
        }

        val translationCount = findViewById<TextView>(R.id.tv_translation_count)
        val count = prefs.translationCount
        translationCount.text = if (count > 0) {
            getString(R.string.dashboard_translations, count)
        } else {
            getString(R.string.dashboard_translations_none)
        }

        findViewById<TextView>(R.id.tv_settings_link).setOnClickListener {
            Navigator.toSettings(this)
        }
    }

    override fun onPermissionResult(granted: Boolean) {
        findViewById<TextView>(R.id.tv_mic_status).let { updateMicStatus(it) }
    }

    private fun updateMicStatus(textView: TextView) {
        if (PermissionHelper.hasRecordAudio(this)) {
            textView.text = getString(R.string.perm_mic_enabled)
            textView.setTextColor(getColorAccent())
        } else {
            textView.text = getString(R.string.perm_mic_disabled)
            textView.setTextColor(getColorMuted())
        }
    }

    private fun getColorAccent(): Int {
        return ContextCompat.getColor(this, R.color.vaani_accent)
    }

    private fun getColorMuted(): Int {
        return ContextCompat.getColor(this, R.color.vaani_text_muted)
    }
}
