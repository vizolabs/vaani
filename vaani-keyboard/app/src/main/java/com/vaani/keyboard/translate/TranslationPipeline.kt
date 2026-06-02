package com.vaani.keyboard.translate

import android.content.Context
import android.util.Log
import com.vaani.keyboard.util.ModelLoader
import java.io.File

class TranslationPipeline(private val context: Context) {

    companion object {
        private const val TAG = "TranslationPipeline"
        private const val MODEL_HI_EN = "marianmt_hi_en.int8.onnx"
        private const val MODEL_MR_EN = "marianmt_mr_en.int8.onnx"

        private const val MAX_RAM_PEAK_BYTES = 67L * 1024 * 1024
    }

    private var currentModel: String? = null
    private var modelBytes: ByteArray? = null

    val isModelLoaded: Boolean
        get() = currentModel != null

    fun loadModel(language: String): Boolean {
        val modelName = when (language) {
            "hi" -> MODEL_HI_EN
            "mr" -> MODEL_MR_EN
            else -> {
                Log.w(TAG, "Unsupported language: $language")
                return false
            }
        }

        unloadModel()

        val modelFile = ModelLoader.modelFile(context, modelName)
        if (!modelFile.exists()) {
            Log.w(TAG, "Model not found: $modelName")
            return false
        }

        if (modelFile.length() > MAX_RAM_PEAK_BYTES) {
            Log.w(TAG, "Model too large for RAM budget: ${modelFile.length()}")
            return false
        }

        currentModel = modelName
        Log.i(TAG, "Loaded model: $modelName (${modelFile.length()} bytes)")
        return true
    }

    fun translate(text: String, sourceLang: String, targetLang: String = "en"): String? {
        val modelName = when (sourceLang) {
            "hi" -> MODEL_HI_EN
            "mr" -> MODEL_MR_EN
            else -> return null
        }

        if (currentModel != modelName) {
            if (!loadModel(sourceLang)) return null
        }

        return runInference(text)
    }

    private fun runInference(text: String): String {
        return text
    }

    fun unloadModel() {
        modelBytes = null
        currentModel = null
        System.gc()
        Log.i(TAG, "Model unloaded, GC triggered")
    }

    fun estimateStorageUsage(): Long {
        return ModelLoader.getStorageUsage(context)
    }
}
