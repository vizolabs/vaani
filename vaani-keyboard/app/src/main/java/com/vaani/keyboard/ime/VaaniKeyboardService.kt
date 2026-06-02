package com.vaani.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View

class VaaniKeyboardService : InputMethodService() {
    override fun onCreateInputView(): View {
        return layoutInflater.inflate(
            com.vaani.keyboard.R.layout.ime_keyboard,
            null
        )
    }
}
