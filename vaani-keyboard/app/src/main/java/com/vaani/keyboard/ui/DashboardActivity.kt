package com.vaani.keyboard.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.vaani.keyboard.R
import com.vaani.keyboard.nav.Navigator
import com.vaani.keyboard.util.ModelManager
import com.vaani.keyboard.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class DashboardActivity : BaseActivity() {

    private val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var modelManager: ModelManager? = null

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

        setupModelSection()
    }

    private fun setupModelSection() {
        val statusText = findViewById<TextView>(R.id.tv_model_status)
        val progressBar = findViewById<ProgressBar>(R.id.progress_model)
        val actionBtn = findViewById<Button>(R.id.btn_model_action)

        actionBtn.visibility = android.view.View.VISIBLE
        updateModelUi(statusText, progressBar, actionBtn)

        actionBtn.setOnClickListener {
            if (prefs.modelDownloaded) {
                confirmDeleteModel(statusText, progressBar, actionBtn)
            } else {
                startDownload(statusText, progressBar, actionBtn)
            }
        }
    }

    private fun updateModelUi(
        statusText: TextView,
        progressBar: ProgressBar,
        actionBtn: Button
    ) {
        if (prefs.modelDownloaded) {
            val modelDir = File(filesDir, "models")
            if (ModelManager.verifyModels(modelDir)) {
                statusText.text = getString(R.string.model_status_ready)
                statusText.setTextColor(getColorAccent())
                actionBtn.text = getString(R.string.model_delete_button)
                progressBar.visibility = android.view.View.GONE
            } else {
                prefs.modelDownloaded = false
                prefs.modelDownloadProgress = 0
                statusText.text = getString(R.string.model_status_not_downloaded)
                statusText.setTextColor(getColorMuted())
                actionBtn.text = getString(R.string.model_download_button)
                progressBar.visibility = android.view.View.GONE
            }
        } else {
            val progress = prefs.modelDownloadProgress
            if (progress > 0 && progress < 100) {
                statusText.text = getString(R.string.model_status_downloading, progress)
                statusText.setTextColor(getColorMuted())
                actionBtn.text = getString(R.string.model_download_button)
                progressBar.visibility = android.view.View.VISIBLE
                progressBar.progress = progress
            } else {
                statusText.text = getString(R.string.model_status_not_downloaded)
                statusText.setTextColor(getColorMuted())
                actionBtn.text = getString(R.string.model_download_button)
                progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun startDownload(
        statusText: TextView,
        progressBar: ProgressBar,
        actionBtn: Button
    ) {
        actionBtn.isEnabled = false
        progressBar.visibility = android.view.View.VISIBLE
        progressBar.progress = 0
        statusText.text = getString(R.string.model_status_downloading, 0)

        val modelDir = File(filesDir, "models")
        modelManager = ModelManager(object : ModelManager.Callback {
            override fun onFileProgress(fileName: String, bytesDownloaded: Long, totalBytes: Long, speedKBps: Long) {
                uiScope.launch {
                    val speed = if (speedKBps > 1024) "${speedKBps / 1024} MB/s" else "${speedKBps} KB/s"
                    val progress = if (totalBytes > 0) (bytesDownloaded * 100 / totalBytes).toInt() else 0
                    statusText.text = "${fileName}: $progress% at $speed"
                }
            }

            override fun onOverallProgress(percent: Int) {
                uiScope.launch {
                    progressBar.progress = percent
                    prefs.modelDownloadProgress = percent
                }
            }

            override fun onVerify(fileName: String) {
                uiScope.launch {
                    statusText.text = getString(R.string.model_status_verifying)
                }
            }

            override fun onComplete(success: Boolean, message: String) {
                uiScope.launch {
                    actionBtn.isEnabled = true
                    if (success) {
                        prefs.modelDownloaded = true
                        prefs.modelDownloadProgress = 100
                        statusText.text = getString(R.string.model_status_ready)
                        statusText.setTextColor(getColorAccent())
                        actionBtn.text = getString(R.string.model_delete_button)
                        progressBar.visibility = android.view.View.GONE
                    } else {
                        prefs.modelDownloadProgress = 0
                        statusText.text = getString(R.string.model_download_failed, message)
                        statusText.setTextColor(getColorMuted())
                        actionBtn.text = getString(R.string.model_download_button)
                        progressBar.visibility = android.view.View.GONE
                    }
                }
            }
        })
        uiScope.launch(Dispatchers.IO) {
            modelManager!!.downloadAll(modelDir)
        }
    }

    private fun confirmDeleteModel(
        statusText: TextView,
        progressBar: ProgressBar,
        actionBtn: Button
    ) {
        AlertDialog.Builder(this)
            .setTitle(R.string.model_delete_confirm_title)
            .setMessage(R.string.model_delete_confirm_message)
            .setPositiveButton(R.string.model_delete_confirm_yes) { _, _ ->
                val modelDir = File(filesDir, "models")
                modelManager?.deleteModels(modelDir)
                prefs.modelDownloaded = false
                prefs.modelDownloadProgress = 0
                statusText.text = getString(R.string.model_status_not_downloaded)
                statusText.setTextColor(getColorMuted())
                actionBtn.text = getString(R.string.model_download_button)
                progressBar.visibility = android.view.View.GONE
            }
            .setNegativeButton(R.string.model_delete_confirm_no, null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        modelManager?.cancel()
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
