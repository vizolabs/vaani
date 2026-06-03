package com.vaani.keyboard.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Prefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vaani_prefs", Context.MODE_PRIVATE)

    var isSetupComplete: Boolean
        get() = prefs.getBoolean(KEY_SETUP_COMPLETE, false)
        set(value) = prefs.edit { putBoolean(KEY_SETUP_COMPLETE, value) }

    var selectedLanguage: String
        get() = prefs.getString(KEY_LANGUAGE, "hi") ?: "hi"
        set(value) = prefs.edit { putString(KEY_LANGUAGE, value) }

    var translationCount: Int
        get() = prefs.getInt(KEY_TRANSLATION_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_TRANSLATION_COUNT, value) }

    fun incrementTranslationCount() {
        translationCount = translationCount + 1
    }

    var lastActiveTimestamp: Long
        get() = prefs.getLong(KEY_LAST_ACTIVE, 0L)
        set(value) = prefs.edit { putLong(KEY_LAST_ACTIVE, value) }

    fun markActive() {
        lastActiveTimestamp = System.currentTimeMillis()
    }

    var keyboardHeightPercent: Int
        get() = prefs.getInt(KEY_KEYBOARD_HEIGHT, 100)
        set(value) = prefs.edit { putInt(KEY_KEYBOARD_HEIGHT, value) }

    var hapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit { putBoolean(KEY_HAPTIC, value) }

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, false)
        set(value) = prefs.edit { putBoolean(KEY_SOUND, value) }

    var modelDownloaded: Boolean
        get() = prefs.getBoolean(KEY_MODEL_DOWNLOADED, false)
        set(value) = prefs.edit { putBoolean(KEY_MODEL_DOWNLOADED, value) }

    var modelDownloadProgress: Int
        get() = prefs.getInt(KEY_MODEL_DOWNLOAD_PROGRESS, 0)
        set(value) = prefs.edit { putInt(KEY_MODEL_DOWNLOAD_PROGRESS, value) }

    fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val KEY_SETUP_COMPLETE = "setup_complete"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_TRANSLATION_COUNT = "translation_count"
        private const val KEY_LAST_ACTIVE = "last_active_timestamp"
        private const val KEY_KEYBOARD_HEIGHT = "keyboard_height"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_SOUND = "key_sound"
        private const val KEY_MODEL_DOWNLOADED = "model_downloaded"
        private const val KEY_MODEL_DOWNLOAD_PROGRESS = "model_download_progress"
    }
}
