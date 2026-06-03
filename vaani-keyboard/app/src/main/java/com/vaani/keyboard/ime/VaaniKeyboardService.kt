package com.vaani.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.vaani.keyboard.R
import com.vaani.keyboard.util.AutoCompleteHelper
import com.vaani.keyboard.util.GrammarEngine
import com.vaani.keyboard.util.PermissionHelper
import com.vaani.keyboard.util.Prefs
import com.vaani.keyboard.util.SpeechRecognizerHelper
import com.vaani.keyboard.util.TranslateEngine
import com.vaani.keyboard.util.Transliterator

class VaaniKeyboardService : InputMethodService() {

    private var isShifted = false
    private var isCaps = false
    private var showSymbols = false
    private val currentInput = StringBuilder()
    private var lastSpaceTime = 0L
    private lateinit var prefs: Prefs
    private lateinit var previewHindi: TextView
    private lateinit var previewEnglish: TextView
    private lateinit var previewHindiSym: TextView
    private lateinit var previewEnglishSym: TextView
    private lateinit var shiftKey: Button
    private lateinit var keyboardContainer: LinearLayout
    private lateinit var qwertyView: View
    private lateinit var symbolsView: View
    private var speechHelper: SpeechRecognizerHelper? = null
    private val keyboardRows = mutableListOf<View>()
    private val sentenceEndChars = setOf(".", "!", "?")
    private val autoSpaceChars = setOf(".", "!", "?", ",", ";", ":")
    private val doubleSpaceMs = 500L

    private val letterKeyIds = listOf(
        R.id.key_q, R.id.key_w, R.id.key_e, R.id.key_r, R.id.key_t,
        R.id.key_y, R.id.key_u, R.id.key_i, R.id.key_o, R.id.key_p,
        R.id.key_a, R.id.key_s, R.id.key_d, R.id.key_f, R.id.key_g,
        R.id.key_h, R.id.key_j, R.id.key_k, R.id.key_l,
        R.id.key_z, R.id.key_x, R.id.key_c, R.id.key_v, R.id.key_b,
        R.id.key_n, R.id.key_m
    )

    private val altChars = mapOf(
        "a" to "áàâäãåæā",
        "e" to "éèêëēėę",
        "i" to "íìîïīį",
        "o" to "óòôöõøōœ",
        "u" to "úùûüū",
        "n" to "ñńņň",
        "s" to "ßśšṣ",
        "c" to "çćčċ",
        "z" to "žźż",
        "1" to "!¹½⅓",
        "2" to "@²⅔",
        "3" to "#³¾",
        "4" to "$¼¤",
        "5" to "%½‰",
        "6" to "^¬⅙",
        "7" to "&⅛⅜",
        "8" to "*⅝⅞",
        "9" to "(⅘",
        "0" to ")°‰",
        "-" to "_–—•",
        "+" to "±÷×=",
        "." to ",…•",
        "/" to "\\",
    )

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
    }

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        currentInput.clear()
        lastSpaceTime = 0L
        isShifted = false
        isCaps = false
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        detectSubtypeLanguage()
    }

    private fun detectSubtypeLanguage() {
        val locale = try {
            @Suppress("DEPRECATION")
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.currentInputMethodSubtype?.locale
        } catch (_: Exception) { null }

        when {
            locale?.startsWith("hi") == true -> prefs.selectedLanguage = "hi"
            locale?.startsWith("mr") == true -> prefs.selectedLanguage = "mr"
            locale?.startsWith("en") == true -> prefs.selectedLanguage = "hinglish"
        }
    }

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
        previewHindiSym = symbolsView.findViewById(R.id.tv_preview_hindi_sym)
        previewEnglishSym = symbolsView.findViewById(R.id.tv_preview_english_sym)
        shiftKey = qwertyView.findViewById(R.id.key_shift)

        setupSuggestionClicks(qwertyView, R.id.suggestion_1, R.id.suggestion_2, R.id.suggestion_3)
        setupSuggestionClicks(symbolsView, R.id.suggestion_sym_1, R.id.suggestion_sym_2, R.id.suggestion_sym_3)

        setupQwertyKeys(qwertyView)
        setupSymbolKeys(symbolsView)
        updateLangKeyText()

        keyboardRows.clear()
        keyboardRows.addAll(listOf(
            qwertyView.findViewById(R.id.kb_row_1),
            qwertyView.findViewById(R.id.kb_row_2),
            qwertyView.findViewById(R.id.kb_row_3),
            qwertyView.findViewById(R.id.kb_row_4),
            symbolsView.findViewById(R.id.kb_sym_row_1),
            symbolsView.findViewById(R.id.kb_sym_row_2),
            symbolsView.findViewById(R.id.kb_sym_row_3),
            symbolsView.findViewById(R.id.kb_sym_row_4),
        ))
        applyKeyboardHeight()

        return keyboardContainer
    }

    private fun setupSuggestionClicks(root: View, id1: Int, id2: Int, id3: Int) {
        root.findViewById<TextView>(id1)?.setOnClickListener { commitSuggestion(0) }
        root.findViewById<TextView>(id2)?.setOnClickListener { commitSuggestion(1) }
        root.findViewById<TextView>(id3)?.setOnClickListener { commitSuggestion(2) }
    }

    private var lastSuggestions: List<String> = emptyList()

    private fun commitSuggestion(index: Int) {
        if (index >= lastSuggestions.size) return
        val word = lastSuggestions[index]
        val ic = currentInputConnection ?: return
        val text = currentInput.toString()
        val lastSpace = text.lastIndexOf(' ')
        val prefixLen = if (lastSpace >= 0) text.length - lastSpace - 1 else text.length

        val beforeCursor = ic.getTextBeforeCursor(prefixLen, 0)?.toString() ?: ""
        if (beforeCursor.lowercase() != text.takeLast(prefixLen).lowercase()) {
            commitWord(word)
            return
        }

        ic.deleteSurroundingText(prefixLen, 0)
        if (lastSpace >= 0) {
            currentInput.delete(currentInput.length - prefixLen, currentInput.length)
            currentInput.append(word)
        } else {
            currentInput.clear()
            currentInput.append(word)
        }
        commitWord(word)
    }

    private fun commitWord(word: String) {
        currentInput.append(" ")
        commitText("$word ")
        updatePreview()
    }

    private fun updateSuggestions() {
        val text = currentInput.toString()
        val lastWord = text.split(" ").lastOrNull { it.isNotBlank() } ?: ""

        val suggestions = if (lastWord.length >= 2) {
            AutoCompleteHelper.suggestions(lastWord, 3)
        } else {
            emptyList()
        }

        lastSuggestions = suggestions
        val qwertySuggestions = listOf(
            qwertyView.findViewById<TextView>(R.id.suggestion_1),
            qwertyView.findViewById<TextView>(R.id.suggestion_2),
            qwertyView.findViewById<TextView>(R.id.suggestion_3),
        )
        val symSuggestions = listOf(
            symbolsView.findViewById<TextView>(R.id.suggestion_sym_1),
            symbolsView.findViewById<TextView>(R.id.suggestion_sym_2),
            symbolsView.findViewById<TextView>(R.id.suggestion_sym_3),
        )

        for (i in 0 until 3) {
            val text = suggestions.getOrNull(i) ?: ""
            qwertySuggestions[i].apply { this.text = text; visibility = if (text.isEmpty()) View.INVISIBLE else View.VISIBLE }
            symSuggestions[i].apply { this.text = text; visibility = if (text.isEmpty()) View.INVISIBLE else View.VISIBLE }
        }
    }

    private fun setupQwertyKeys(root: View) {
        setKeyListener(root, R.id.key_q, "q"); setKeyListener(root, R.id.key_w, "w")
        setKeyListener(root, R.id.key_e, "e"); setKeyListener(root, R.id.key_r, "r")
        setKeyListener(root, R.id.key_t, "t"); setKeyListener(root, R.id.key_y, "y")
        setKeyListener(root, R.id.key_u, "u"); setKeyListener(root, R.id.key_i, "i")
        setKeyListener(root, R.id.key_o, "o"); setKeyListener(root, R.id.key_p, "p")
        setKeyListener(root, R.id.key_backspace, "backspace")
        setKeyListener(root, R.id.key_a, "a"); setKeyListener(root, R.id.key_s, "s")
        setKeyListener(root, R.id.key_d, "d"); setKeyListener(root, R.id.key_f, "f")
        setKeyListener(root, R.id.key_g, "g"); setKeyListener(root, R.id.key_h, "h")
        setKeyListener(root, R.id.key_j, "j"); setKeyListener(root, R.id.key_k, "k")
        setKeyListener(root, R.id.key_l, "l"); setKeyListener(root, R.id.key_enter, "enter")
        setKeyListener(root, R.id.key_shift, "shift")
        setKeyListener(root, R.id.key_z, "z"); setKeyListener(root, R.id.key_x, "x")
        setKeyListener(root, R.id.key_c, "c"); setKeyListener(root, R.id.key_v, "v")
        setKeyListener(root, R.id.key_b, "b"); setKeyListener(root, R.id.key_n, "n")
        setKeyListener(root, R.id.key_m, "m"); setKeyListener(root, R.id.key_comma, ",")
        setKeyListener(root, R.id.key_dot, "."); setKeyListener(root, R.id.key_slash, "/")
        setKeyListener(root, R.id.key_symbol, "toggle_symbols")
        setKeyListener(root, R.id.key_lang, "toggle_lang")
        setKeyListener(root, R.id.key_mic, "mic")
        setKeyListener(root, R.id.key_space, " ")
        setKeyListener(root, R.id.key_period, ".")
        setKeyListener(root, R.id.key_send, "send")
    }

    private fun setupSymbolKeys(root: View) {
        setKeyListener(root, R.id.key_1, "1"); setKeyListener(root, R.id.key_2, "2")
        setKeyListener(root, R.id.key_3, "3"); setKeyListener(root, R.id.key_4, "4")
        setKeyListener(root, R.id.key_5, "5"); setKeyListener(root, R.id.key_6, "6")
        setKeyListener(root, R.id.key_7, "7"); setKeyListener(root, R.id.key_8, "8")
        setKeyListener(root, R.id.key_9, "9"); setKeyListener(root, R.id.key_0, "0")
        setKeyListener(root, R.id.key_at, "@"); setKeyListener(root, R.id.key_hash, "#")
        setKeyListener(root, R.id.key_dollar, "$"); setKeyListener(root, R.id.key_percent, "%")
        setKeyListener(root, R.id.key_amp, "&"); setKeyListener(root, R.id.key_star, "*")
        setKeyListener(root, R.id.key_minus, "-"); setKeyListener(root, R.id.key_plus, "+")
        setKeyListener(root, R.id.key_lparen, "("); setKeyListener(root, R.id.key_rparen, ")")
        setKeyListener(root, R.id.key_lbracket, "["); setKeyListener(root, R.id.key_rbracket, "]")
        setKeyListener(root, R.id.key_lbrace, "{"); setKeyListener(root, R.id.key_rbrace, "}")
        setKeyListener(root, R.id.key_lt, "<"); setKeyListener(root, R.id.key_gt, ">")
        setKeyListener(root, R.id.key_eq, "="); setKeyListener(root, R.id.key_pipe, "|")
        setKeyListener(root, R.id.key_tilde, "~"); setKeyListener(root, R.id.key_backtick, "`")
        setKeyListener(root, R.id.key_bksp_sym, "backspace")
        setKeyListener(root, R.id.key_abc, "toggle_symbols")
        setKeyListener(root, R.id.key_lang_sym, "toggle_lang")
        setKeyListener(root, R.id.key_mic_sym, "mic")
        setKeyListener(root, R.id.key_space_sym, " ")
        setKeyListener(root, R.id.key_period_sym, ".")
        setKeyListener(root, R.id.key_send_sym, "send")
    }

    private fun applyKeyboardHeight() {
        val percent = prefs.keyboardHeightPercent
        val basePx = resources.getDimension(R.dimen.kb_key_height)
        val targetPx = (basePx * percent / 100f).toInt()
        for (row in keyboardRows) {
            if (row != null) {
                row.layoutParams.height = targetPx
                row.requestLayout()
            }
        }
    }

    private fun setKeyListener(root: View, id: Int, value: String) {
        val key = root.findViewById<Button>(id) ?: return
        key.setOnClickListener {
            if (prefs.hapticEnabled) {
                key.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
            if (prefs.soundEnabled) {
                (getSystemService(AUDIO_SERVICE) as? AudioManager)?.playSoundEffect(AudioManager.FX_KEY_CLICK)
            }
            onKeyPressed(value)
        }
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
            "toggle_lang" -> handleLangToggle()
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
            else -> {
                val alt = altChars[value]
                if (alt != null && alt.isNotEmpty()) {
                    commitText(alt.first().toString())
                    currentInput.append(alt.first())
                    updatePreview()
                }
            }
        }
    }

    private fun toggleSymbols() {
        showSymbols = !showSymbols
        if (showSymbols) {
            qwertyView.visibility = View.GONE
            symbolsView.visibility = View.VISIBLE
        } else {
            symbolsView.visibility = View.GONE
            qwertyView.visibility = View.VISIBLE
            updateShiftKeyAppearance()
            updateKeyLabels()
        }
    }

    private fun handleChar(c: String) {
        val char = if (isShifted || isCaps) c.uppercase() else c
        commitText(char)
        currentInput.append(char)
        val sentenceEnd = char in sentenceEndChars
        val autoSpace = char in autoSpaceChars
        if (autoSpace) {
            commitText(" ")
            currentInput.append(" ")
        }
        updatePreview()
        if (isShifted && !isCaps && !sentenceEnd) {
            isShifted = false
            updateShiftKeyAppearance()
        }
    }

    private fun handleSpace() {
        val now = System.currentTimeMillis()
        if (now - lastSpaceTime < doubleSpaceMs) {
            val ic = currentInputConnection ?: return
            ic.deleteSurroundingText(1, 0)
            if (currentInput.isNotEmpty()) currentInput.deleteCharAt(currentInput.length - 1)
            commitText(". ")
            currentInput.append(". ")
            lastSpaceTime = 0L
            isShifted = true
            updateShiftKeyAppearance()
            updateKeyLabels()
        } else {
            commitText(" ")
            currentInput.append(" ")
        }
        lastSpaceTime = now
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

    private fun handleLangToggle() {
        prefs.selectedLanguage = when (prefs.selectedLanguage) {
            "hi" -> "mr"
            "mr" -> "hinglish"
            else -> "hi"
        }
        updateLangKeyText()
        updatePreview()
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

        val lang = when (prefs.selectedLanguage) {
            "hi" -> "hi-IN"
            "mr" -> "mr-IN"
            "hinglish" -> "en-IN"
            else -> "hi-IN"
        }

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
            }
        )
        speechHelper?.startListening(lang)
    }

    private fun handleSend() {
        val rawText = currentInput.toString().trim()
        if (rawText.isEmpty()) return
        val ic = currentInputConnection ?: return

        val english = TranslateEngine.translate(rawText)

        ic.deleteSurroundingText(currentInput.length, 0)
        ic.commitText(english, 1)
        currentInput.clear()
        prefs.incrementTranslationCount()
        prefs.markActive()
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
            val lower = btn.text.toString().first().lowercaseChar().toString()
            btn.text = if (isCaps || isShifted) lower.uppercase() else lower
        }
    }

    private fun updateLangKeyText() {
        val label = when (prefs.selectedLanguage) {
            "hi" -> getString(R.string.lang_label_hi)
            "mr" -> getString(R.string.lang_label_mr)
            else -> getString(R.string.lang_label_en)
        }
        qwertyView.findViewById<Button>(R.id.key_lang)?.text = label
        symbolsView.findViewById<Button>(R.id.key_lang_sym)?.text = label
    }

    private fun updatePreview() {
        val text = currentInput.toString().trim()
        if (text.isEmpty()) {
            previewHindi.text = ""
            previewEnglish.text = ""
            if (::previewHindiSym.isInitialized) {
                previewHindiSym.text = ""
                previewEnglishSym.text = ""
            }
            lastSuggestions = emptyList()
            updateSuggestions()
            return
        }

        val devanagari = Transliterator.transliterate(text)
        previewHindi.text = devanagari
        if (::previewHindiSym.isInitialized) {
            previewHindiSym.text = devanagari
        }

        val english = TranslateEngine.translate(text)
        previewEnglish.text = "→ $english"
        if (::previewEnglishSym.isInitialized) {
            previewEnglishSym.text = "→ $english"
        }

        updateSuggestions()
    }
}
