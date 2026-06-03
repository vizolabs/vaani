package com.vaani.keyboard.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.vaani.keyboard.R
import com.vaani.keyboard.nav.Navigator

class SetupActivity : BaseActivity() {

    private var currentStep = 0
    private var selectedLanguage = "hi"
    private lateinit var stepContent: FrameLayout
    private lateinit var btnBack: Button
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var stepLabel: TextView

    private val totalSteps = 5
    private val stepLayouts = listOf(
        R.layout.setup_step_welcome,
        R.layout.setup_step_enable_keyboard,
        R.layout.setup_step_switch_keyboard,
        R.layout.setup_step_choose_language,
        R.layout.setup_step_ready,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        stepContent = findViewById(R.id.step_content)
        btnBack = findViewById(R.id.btn_back)
        btnNext = findViewById(R.id.btn_next)
        progressBar = findViewById(R.id.progress_bar)
        stepLabel = findViewById(R.id.tv_step_label)

        btnBack.setOnClickListener { goBack() }
        btnNext.setOnClickListener { goNext() }

        showStep(0)
    }

    override fun onBackPressed() {
        if (currentStep > 0) {
            goBack()
        } else {
            moveTaskToBack(true)
        }
    }

    private fun showStep(index: Int) {
        currentStep = index
        stepContent.removeAllViews()
        layoutInflater.inflate(stepLayouts[index], stepContent)
        updateNavigation()
        setupStepListeners()
    }

    private fun goBack() {
        if (currentStep > 0) showStep(currentStep - 1)
    }

    private fun goNext() {
        val next = currentStep + 1
        if (next < totalSteps) {
            showStep(next)
        } else {
            finishSetup()
        }
    }

    private fun finishSetup() {
        prefs.isSetupComplete = true
        prefs.selectedLanguage = selectedLanguage
        Navigator.toDashboard(this)
    }

    private fun updateNavigation() {
        val progress = ((currentStep + 1) * 100) / totalSteps
        progressBar.progress = progress
        stepLabel.text = getString(R.string.setup_step_of, currentStep + 1, totalSteps)

        btnBack.visibility = if (currentStep > 0) android.view.View.VISIBLE else android.view.View.GONE
        btnNext.text = if (currentStep == totalSteps - 1) {
            getString(R.string.setup_btn_finish)
        } else {
            getString(R.string.setup_btn_next)
        }
    }

    private fun setupStepListeners() {
        val openKeyboardSettings = findViewById<Button>(R.id.btn_open_keyboard_settings)
        openKeyboardSettings?.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        val openDefaultKeyboard = findViewById<Button>(R.id.btn_open_default_keyboard_settings)
        openDefaultKeyboard?.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        val btnHindi = findViewById<Button>(R.id.btn_lang_hindi)
        val btnMarathi = findViewById<Button>(R.id.btn_lang_marathi)
        val btnHinglish = findViewById<Button>(R.id.btn_lang_hinglish)

        if (btnHindi != null) {
            highlightLang(btnHindi, selectedLanguage == "hi")
            btnHindi.setOnClickListener {
                selectedLanguage = "hi"
                highlightLang(btnHindi, true)
                highlightLang(btnMarathi, false)
                highlightLang(btnHinglish, false)
            }
        }
        if (btnMarathi != null) {
            highlightLang(btnMarathi, selectedLanguage == "mr")
            btnMarathi.setOnClickListener {
                selectedLanguage = "mr"
                highlightLang(btnHindi, false)
                highlightLang(btnMarathi, true)
                highlightLang(btnHinglish, false)
            }
        }
        if (btnHinglish != null) {
            highlightLang(btnHinglish, selectedLanguage == "hinglish")
            btnHinglish.setOnClickListener {
                selectedLanguage = "hinglish"
                highlightLang(btnHindi, false)
                highlightLang(btnMarathi, false)
                highlightLang(btnHinglish, true)
            }
        }
    }

    private fun highlightLang(button: Button?, selected: Boolean) {
        if (button == null) return
        button.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (selected) R.color.vaani_accent else R.color.vaani_surface_card
        )
    }
}
