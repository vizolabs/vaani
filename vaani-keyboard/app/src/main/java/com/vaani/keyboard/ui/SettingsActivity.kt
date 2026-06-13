package com.vaani.keyboard.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.vaani.keyboard.BuildConfig
import com.vaani.keyboard.R
import com.vaani.keyboard.util.ModelManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class SettingsActivity : BaseActivity() {

    private val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var modelManager: ModelManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupLanguageButtons()
        setupKeyboardHeight()
        setupHapticToggle()
        setupSoundToggle()
        setupModelSection()
        findViewById<TextView>(R.id.tv_settings_about)?.text =
            getString(R.string.settings_about_text, BuildConfig.VERSION_NAME)
    }

    private fun setupLanguageButtons() {
        val currentLang = prefs.selectedLanguage

        val btnHi = findViewById<Button>(R.id.btn_settings_lang_hi)
        val btnMr = findViewById<Button>(R.id.btn_settings_lang_mr)
        val btnEn = findViewById<Button>(R.id.btn_settings_lang_en)

        highlightLang(btnHi, currentLang == "hi")
        highlightLang(btnMr, currentLang == "mr")
        highlightLang(btnEn, currentLang == "hinglish")

        btnHi.setOnClickListener {
            prefs.selectedLanguage = "hi"
            highlightLang(btnHi, true); highlightLang(btnMr, false); highlightLang(btnEn, false)
            showSaved()
        }
        btnMr.setOnClickListener {
            prefs.selectedLanguage = "mr"
            highlightLang(btnHi, false); highlightLang(btnMr, true); highlightLang(btnEn, false)
            showSaved()
        }
        btnEn.setOnClickListener {
            prefs.selectedLanguage = "hinglish"
            highlightLang(btnHi, false); highlightLang(btnMr, false); highlightLang(btnEn, true)
            showSaved()
        }
    }

    private fun highlightLang(button: Button, selected: Boolean) {
        button.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (selected) R.color.vaani_accent else R.color.vaani_surface_card
        )
    }

    private fun setupKeyboardHeight() {
        val seekBar = findViewById<SeekBar>(R.id.seek_keyboard_height)
        val label = findViewById<TextView>(R.id.tv_keyboard_height_value)

        val current = prefs.keyboardHeightPercent
        seekBar.progress = current - 60
        label.text = "$current%"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                val percent = progress + 60
                label.text = "$percent%"
                prefs.keyboardHeightPercent = percent
            }
            override fun onStartTrackingTouch(seek: SeekBar?) {}
            override fun onStopTrackingTouch(seek: SeekBar?) {}
        })
    }

    private fun setupHapticToggle() {
        val switch = findViewById<SwitchCompat>(R.id.switch_haptic)
        switch.isChecked = prefs.hapticEnabled
        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.hapticEnabled = isChecked
        }
    }

    private fun setupSoundToggle() {
        val switch = findViewById<SwitchCompat>(R.id.switch_sound)
        switch.isChecked = prefs.soundEnabled
        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.soundEnabled = isChecked
        }
    }

    private fun setupModelSection() {
        val statusText = findViewById<TextView>(R.id.tv_settings_model_status)
        val progressBar = findViewById<ProgressBar>(R.id.progress_settings_model)
        val actionBtn = findViewById<Button>(R.id.btn_settings_model_action)

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
        modelManager = ModelManager(this, object : ModelManager.Callback {
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
                        prefs.modelVersion = prefs.modelVersion + 1
                        statusText.text = getString(R.string.model_download_success)
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

    private fun getColorAccent(): Int {
        return ContextCompat.getColor(this, R.color.vaani_accent)
    }

    private fun getColorMuted(): Int {
        return ContextCompat.getColor(this, R.color.vaani_text_muted)
    }

    private fun showSaved() {
        Toast.makeText(this, R.string.settings_lang_saved, Toast.LENGTH_SHORT).show()
    }
}
