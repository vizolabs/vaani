package com.vaani.keyboard.util

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object ModelLoader {

    private const val MODELS_DIR = "models"

    fun modelFile(context: Context, modelName: String): File {
        val dir = File(context.filesDir, MODELS_DIR)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, modelName)
    }

    fun isModelLoaded(context: Context, modelName: String): Boolean {
        return modelFile(context, modelName).exists()
    }

    fun copyFromAssets(context: Context, assetName: String, modelName: String): Boolean {
        val output = modelFile(context, modelName)
        if (output.exists()) return true

        return try {
            context.assets.open(assetName).use { input ->
                FileOutputStream(output).use { outputStream ->
                    input.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteModel(context: Context, modelName: String): Boolean {
        return modelFile(context, modelName).delete()
    }

    fun getModelSize(context: Context, modelName: String): Long {
        val file = modelFile(context, modelName)
        return if (file.exists()) file.length() else 0L
    }

    fun getStorageUsage(context: Context): Long {
        val dir = File(context.filesDir, MODELS_DIR)
        if (!dir.exists()) return 0L
        return dir.listFiles()?.sumOf { it.length() } ?: 0L
    }
}
