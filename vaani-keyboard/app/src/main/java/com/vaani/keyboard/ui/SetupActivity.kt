package com.vaani.keyboard.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.vaani.keyboard.R
import com.vaani.keyboard.nav.Navigator

class SetupActivity : BaseActivity() {

    private var currentStep = 0
    private lateinit var stepContent: FrameLayout
    private lateinit var btnBack: Button
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var stepLabel: TextView

    private val totalSteps = 2
    private val stepLayouts = listOf(
        R.layout.setup_step_welcome,
        R.layout.setup_step_enable_keyboard,
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
        prefs.selectedLanguage = "hi"
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
        val openSettings = findViewById<Button>(R.id.btn_open_keyboard_settings)
        openSettings?.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
    }
}
