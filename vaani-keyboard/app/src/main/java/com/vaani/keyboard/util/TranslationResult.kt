package com.vaani.keyboard.util

sealed class TranslationResult {
    data class Success(val text: String) : TranslationResult()
    data class Error(val message: String) : TranslationResult()
}
