package com.vaani.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.vaani.keyboard.R
import com.vaani.keyboard.util.PermissionHelper
import com.vaani.keyboard.util.SpeechRecognizerHelper
import com.vaani.keyboard.util.Transliterator

class VaaniKeyboardService : InputMethodService() {

    private var isShifted = false
    private var isCaps = false
    private var showSymbols = false
    private val currentInput = StringBuilder()
    private lateinit var previewHindi: TextView
    private lateinit var previewEnglish: TextView
    private lateinit var shiftKey: Button
    private lateinit var micKey: Button
    private lateinit var keyboardContainer: LinearLayout
    private lateinit var qwertyView: View
    private lateinit var symbolsView: View
    private var speechHelper: SpeechRecognizerHelper? = null

    private val letterKeyIds = listOf(
        R.id.key_q, R.id.key_w, R.id.key_e, R.id.key_r, R.id.key_t,
        R.id.key_y, R.id.key_u, R.id.key_i, R.id.key_o, R.id.key_p,
        R.id.key_a, R.id.key_s, R.id.key_d, R.id.key_f, R.id.key_g,
        R.id.key_h, R.id.key_j, R.id.key_k, R.id.key_l,
        R.id.key_z, R.id.key_x, R.id.key_c, R.id.key_v, R.id.key_b,
        R.id.key_n, R.id.key_m
    )

    override fun onCreateInputView(): View {
        keyboardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        qwertyView = layoutInflater.inflate(R.layout.ime_keyboard, keyboardContainer, false)
        symbolsView = layoutInflater.inflate(R.layout.ime_keyboard_symbols, keyboardContainer, false)

        keyboardContainer.addView(qwertyView)
        keyboardContainer.addView(symbolsView)
        symbolsView.visibility = View.GONE

        previewHindi = qwertyView.findViewById(R.id.tv_preview_hindi)
        previewEnglish = qwertyView.findViewById(R.id.tv_preview_english)
        shiftKey = qwertyView.findViewById(R.id.key_shift)
        micKey = qwertyView.findViewById(R.id.key_mic)

        setupQwertyKeys(qwertyView)
        setupSymbolKeys(symbolsView)

        return keyboardContainer
    }

    private fun setupQwertyKeys(root: View) {
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
        setKeyListener(root, R.id.key_symbol, "toggle_symbols")
        setKeyListener(root, R.id.key_mic, "mic")
        setKeyListener(root, R.id.key_space, " ")
        setKeyListener(root, R.id.key_period, ".")
        setKeyListener(root, R.id.key_send, "send")
    }

    private fun setupSymbolKeys(root: View) {
        setKeyListener(root, R.id.key_1, "1")
        setKeyListener(root, R.id.key_2, "2")
        setKeyListener(root, R.id.key_3, "3")
        setKeyListener(root, R.id.key_4, "4")
        setKeyListener(root, R.id.key_5, "5")
        setKeyListener(root, R.id.key_6, "6")
        setKeyListener(root, R.id.key_7, "7")
        setKeyListener(root, R.id.key_8, "8")
        setKeyListener(root, R.id.key_9, "9")
        setKeyListener(root, R.id.key_0, "0")
        setKeyListener(root, R.id.key_at, "@")
        setKeyListener(root, R.id.key_hash, "#")
        setKeyListener(root, R.id.key_dollar, "$")
        setKeyListener(root, R.id.key_percent, "%")
        setKeyListener(root, R.id.key_amp, "&")
        setKeyListener(root, R.id.key_star, "*")
        setKeyListener(root, R.id.key_minus, "-")
        setKeyListener(root, R.id.key_plus, "+")
        setKeyListener(root, R.id.key_lparen, "(")
        setKeyListener(root, R.id.key_rparen, ")")
        setKeyListener(root, R.id.key_lbracket, "[")
        setKeyListener(root, R.id.key_rbracket, "]")
        setKeyListener(root, R.id.key_lbrace, "{")
        setKeyListener(root, R.id.key_rbrace, "}")
        setKeyListener(root, R.id.key_lt, "<")
        setKeyListener(root, R.id.key_gt, ">")
        setKeyListener(root, R.id.key_eq, "=")
        setKeyListener(root, R.id.key_pipe, "|")
        setKeyListener(root, R.id.key_tilde, "~")
        setKeyListener(root, R.id.key_backtick, "`")
        setKeyListener(root, R.id.key_bksp_sym, "backspace")
        setKeyListener(root, R.id.key_abc, "toggle_symbols")
        setKeyListener(root, R.id.key_mic_sym, "mic")
        setKeyListener(root, R.id.key_space_sym, " ")
        setKeyListener(root, R.id.key_period_sym, ".")
        setKeyListener(root, R.id.key_send_sym, "send")
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
            "toggle_symbols" -> toggleSymbols()
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
            "mic" -> {
                if (!PermissionHelper.hasRecordAudio(this)) {
                    PermissionHelper.openAppSettings(this)
                }
            }
        }
    }

    private fun toggleSymbols() {
        showSymbols = !showSymbols
        if (showSymbols) {
            qwertyView.visibility = View.GONE
            symbolsView.visibility = View.VISIBLE
            micKey = symbolsView.findViewById(R.id.key_mic_sym)
        } else {
            symbolsView.visibility = View.GONE
            qwertyView.visibility = View.VISIBLE
            micKey = qwertyView.findViewById(R.id.key_mic)
            updateShiftKeyAppearance()
            updateKeyLabels()
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

    private fun handleMic() {
        if (!PermissionHelper.hasRecordAudio(this)) {
            previewHindi.text = getString(R.string.perm_mic_denied_message)
            previewEnglish.text = ""
            return
        }
        if (!SpeechRecognizerHelper.isAvailable(this)) {
            previewHindi.text = getString(R.string.voice_unavailable)
            previewEnglish.text = ""
            return
        }

        val language = "hi-IN"

        previewHindi.text = getString(R.string.voice_listening)
        previewEnglish.text = ""

        speechHelper?.stopListening()
        speechHelper = SpeechRecognizerHelper(
            context = this,
            onResult = { text ->
                commitText("$text ")
                currentInput.append("$text ")
                updatePreview()
            },
            onError = { error ->
                previewHindi.text = getString(R.string.voice_error, error)
                previewEnglish.text = ""
                keyboardContainer.postDelayed({ updatePreview() }, 2000)
            }
        )
        speechHelper?.startListening(language)
    }

    private fun handleSend() {
        val text = currentInput.toString().trim()
        if (text.isEmpty()) return
        val ic = currentInputConnection ?: return

        val transliterated = Transliterator.transliterate(text)
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

    override fun onFinishInput() {
        super.onFinishInput()
        speechHelper?.stopListening()
        speechHelper = null
    }

    override fun onDestroy() {
        super.onDestroy()
        speechHelper?.stopListening()
        speechHelper = null
    }

    private fun updateShiftKeyAppearance() {
        if (!::shiftKey.isInitialized) return
        val color = if (isCaps) {
            ContextCompat.getColor(this, R.color.vaani_accent)
        } else {
            ContextCompat.getColor(this, R.color.kb_key_text)
        }
        shiftKey.setTextColor(color)
    }

    private fun updateKeyLabels() {
        for (id in letterKeyIds) {
            val btn = qwertyView.findViewById<Button>(id) ?: continue
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
