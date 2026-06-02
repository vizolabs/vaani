package com.vaani.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.vaani.keyboard.R
import com.vaani.keyboard.util.Transliterator

class VaaniKeyboardService : InputMethodService() {

    private var isShifted = false
    private var isCaps = false
    private val currentInput = StringBuilder()
    private lateinit var previewHindi: TextView
    private lateinit var previewEnglish: TextView
    private lateinit var shiftKey: Button
    private lateinit var keyboardView: ViewGroup

    private val letterKeyIds = listOf(
        R.id.key_q, R.id.key_w, R.id.key_e, R.id.key_r, R.id.key_t,
        R.id.key_y, R.id.key_u, R.id.key_i, R.id.key_o, R.id.key_p,
        R.id.key_a, R.id.key_s, R.id.key_d, R.id.key_f, R.id.key_g,
        R.id.key_h, R.id.key_j, R.id.key_k, R.id.key_l,
        R.id.key_z, R.id.key_x, R.id.key_c, R.id.key_v, R.id.key_b,
        R.id.key_n, R.id.key_m
    )

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.ime_keyboard, null) as ViewGroup
        previewHindi = keyboardView.findViewById(R.id.tv_preview_hindi)
        previewEnglish = keyboardView.findViewById(R.id.tv_preview_english)
        shiftKey = keyboardView.findViewById(R.id.key_shift)
        setupKeys(keyboardView)
        return keyboardView
    }

    private fun setupKeys(root: View) {
        setKeyListener(root, R.id.key_q, "q")
        setKeyListener(root, R.id.key_w, "w")
        setKeyListener(root, R.id.key_e, "e")
        setKeyListener(root, R.id.key_r, "r")
        setKeyListener(root, R.id.key_t, "t")
        setKeyListener(root, R.id.key_y, "y")
        setKeyListener(root, R.id.key_u, "u")
        setKeyListener(root, R.id.key_i, "i")
        setKeyListener(root, R.id.key_o, "o")
        setKeyListener(root, R.id.key_p, "p")
        setKeyListener(root, R.id.key_backspace, "backspace")
        setKeyListener(root, R.id.key_a, "a")
        setKeyListener(root, R.id.key_s, "s")
        setKeyListener(root, R.id.key_d, "d")
        setKeyListener(root, R.id.key_f, "f")
        setKeyListener(root, R.id.key_g, "g")
        setKeyListener(root, R.id.key_h, "h")
        setKeyListener(root, R.id.key_j, "j")
        setKeyListener(root, R.id.key_k, "k")
        setKeyListener(root, R.id.key_l, "l")
        setKeyListener(root, R.id.key_enter, "enter")
        setKeyListener(root, R.id.key_shift, "shift")
        setKeyListener(root, R.id.key_z, "z")
        setKeyListener(root, R.id.key_x, "x")
        setKeyListener(root, R.id.key_c, "c")
        setKeyListener(root, R.id.key_v, "v")
        setKeyListener(root, R.id.key_b, "b")
        setKeyListener(root, R.id.key_n, "n")
        setKeyListener(root, R.id.key_m, "m")
        setKeyListener(root, R.id.key_comma, ",")
        setKeyListener(root, R.id.key_dot, ".")
        setKeyListener(root, R.id.key_slash, "/")
        setKeyListener(root, R.id.key_symbol, "symbol")
        setKeyListener(root, R.id.key_mic, "mic")
        setKeyListener(root, R.id.key_space, " ")
        setKeyListener(root, R.id.key_period, ".")
        setKeyListener(root, R.id.key_send, "send")
    }

    private fun setKeyListener(root: View, id: Int, value: String) {
        val key = root.findViewById<Button>(id) ?: return
        key.setOnClickListener { onKeyPressed(value) }
        key.setOnLongClickListener {
            onKeyLongPressed(value)
            true
        }
    }

    private fun onKeyPressed(value: String) {
        when (value) {
            "backspace" -> handleBackspace()
            "enter" -> handleEnter()
            "shift" -> handleShift()
            "symbol" -> handleSymbol()
            "mic" -> handleMic()
            "send" -> handleSend()
            " " -> handleSpace()
            else -> handleChar(value)
        }
    }

    private fun onKeyLongPressed(value: String) {
        when (value) {
            "shift" -> handleCapsLock()
            "backspace" -> { clearAll(); updatePreview() }
            "." -> commitText("...")
        }
    }

    private fun handleChar(c: String) {
        val char = if (isShifted || isCaps) c.uppercase() else c
        commitText(char)
        currentInput.append(c)

        updatePreview()

        if (isShifted && !isCaps) {
            isShifted = false
            updateShiftKeyAppearance()
        }
    }

    private fun handleSpace() {
        commitText(" ")
        currentInput.append(" ")
        updatePreview()
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        ic.deleteSurroundingText(1, 0)
        if (currentInput.isNotEmpty()) {
            currentInput.deleteCharAt(currentInput.length - 1)
        }
        updatePreview()
    }

    private fun handleEnter() {
        currentInputConnection?.commitText("\n", 1)
        currentInput.clear()
        updatePreview()
    }

    private fun handleShift() {
        isShifted = !isShifted
        if (isShifted) isCaps = false
        updateShiftKeyAppearance()
        updateKeyLabels()
    }

    private fun handleCapsLock() {
        isCaps = !isCaps
        isShifted = isCaps
        updateShiftKeyAppearance()
        updateKeyLabels()
    }

    private fun handleSymbol() {
    }

    private fun handleMic() {
    }

    private fun handleSend() {
        val text = currentInput.toString().trim()
        if (text.isEmpty()) return
        val ic = currentInputConnection ?: return

        val transliterated = Transliterator.transliterate(text)
        val currentText = ic.getTextBeforeCursor(currentInput.length, 0) ?: return

        ic.deleteSurroundingText(currentInput.length, 0)
        ic.commitText(transliterated, 1)
        currentInput.clear()
        updatePreview()
    }

    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun clearAll() {
        val ic = currentInputConnection ?: return
        ic.deleteSurroundingText(currentInput.length, 0)
        currentInput.clear()
    }

    private fun updateShiftKeyAppearance() {
        if (isCaps) {
            shiftKey.setTextColor(
                androidx.core.content.ContextCompat.getColor(this, R.color.vaani_accent)
            )
        } else {
            shiftKey.setTextColor(
                androidx.core.content.ContextCompat.getColor(this, R.color.kb_key_text)
            )
        }
    }

    private fun updateKeyLabels() {
        for (id in letterKeyIds) {
            val btn = keyboardView.findViewById<Button>(id) ?: continue
            val original = btn.text.toString()
            btn.text = if (isCaps || isShifted) original.uppercase() else original.lowercase()
        }
    }

    private fun updatePreview() {
        val text = currentInput.toString().trim()
        if (text.isEmpty()) {
            previewHindi.text = ""
            previewEnglish.text = ""
            return
        }

        val devanagari = Transliterator.transliterate(text)
        previewHindi.text = devanagari

        val lastWord = text.split(" ").lastOrNull() ?: ""
        val translated = Transliterator.transliterate(lastWord)
        previewEnglish.text = "→ $translated"
    }
}
