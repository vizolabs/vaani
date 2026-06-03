package com.vaani.keyboard.ui

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.vaani.keyboard.R

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupLanguageButtons()
        setupKeyboardHeight()
    }

    private fun setupLanguageButtons() {
        val currentLang = prefs.selectedLanguage

        val btnHi = findViewById<Button>(R.id.btn_settings_lang_hi)
        val btnMr = findViewById<Button>(R.id.btn_settings_lang_mr)
        val btnEn = findViewById<Button>(R.id.btn_settings_lang_en)

        highlightLang(btnHi, currentLang == "hi")
        highlightLang(btnMr, currentLang == "mr")
        highlightLang(btnEn, currentLang == "en")

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
            prefs.selectedLanguage = "en"
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

    private fun showSaved() {
        Toast.makeText(this, R.string.settings_lang_saved, Toast.LENGTH_SHORT).show()
    }
}
