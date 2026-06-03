package com.vaani.keyboard.util

import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ModelManager(private val callback: Callback) {

    interface Callback {
        fun onProgress(percent: Int)
        fun onComplete(success: Boolean, message: String)
    }

    companion object {
        private const val TAG = "ModelManager"
        private const val BUFFER_SIZE = 8192

        private val BASE = "https://huggingface.co/Xenova/nllb-200-distilled-600M/resolve/main/onnx"
        private val SPM_BASE = "https://huggingface.co/facebook/nllb-200-distilled-600M/resolve/main"

        val FILES = listOf(
            ModelFile("encoder_model_quantized.onnx", "$BASE/encoder_model_quantized.onnx", 419_000_000),
            ModelFile("decoder_with_past_model_quantized.onnx", "$BASE/decoder_with_past_model_quantized.onnx", 445_000_000),
            ModelFile("sentencepiece.bpe.model", "$SPM_BASE/sentencepiece.bpe.model", 4_000_000)
        )

        data class ModelFile(val name: String, val url: String, val sizeEstimate: Int)
    }

    private var cancelled = false

    fun cancel() {
        cancelled = true
    }

    suspend fun downloadAll(modelDir: File) = withContext(Dispatchers.IO) {
        cancelled = false
        modelDir.mkdirs()

        val totalFiles = FILES.size
        var completedFiles = 0

        for (file in FILES) {
            if (cancelled) {
                callback.onComplete(false, "Download cancelled")
                return@withContext
            }
            val target = File(modelDir, file.name)
            if (target.exists() && target.length() > 0) {
                completedFiles++
                updateProgress(completedFiles, totalFiles)
                continue
            }
            try {
                downloadFile(file, target) { percent ->
                    val overall = ((completedFiles.toFloat() + percent.toFloat() / 100f) / totalFiles.toFloat() * 100).toInt()
                    callback.onProgress(overall)
                }
                completedFiles++
                updateProgress(completedFiles, totalFiles)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download ${file.name}: ${e.message}")
                callback.onComplete(false, "Failed to download ${file.name}: ${e.message}")
                return@withContext
            }
        }

        callback.onComplete(true, "All model files downloaded successfully")
    }

    private fun updateProgress(completed: Int, total: Int) {
        val percent = (completed.toFloat() / total.toFloat() * 100).toInt()
        callback.onProgress(percent)
    }

    private fun downloadFile(
        file: ModelFile,
        target: File,
        onProgress: (percent: Int) -> Unit
    ) {
        val url = URL(file.url)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 30000
        conn.instanceFollowRedirects = true

        try {
            conn.connect()
            val contentLength = conn.contentLength

            FileOutputStream(target).use { fos ->
                val input = conn.inputStream
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                var totalBytes = 0L

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    if (cancelled) {
                        target.delete()
                        return
                    }
                    fos.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                    if (contentLength > 0) {
                        val percent = ((totalBytes.toFloat() / contentLength.toFloat()) * 100).toInt()
                        onProgress(percent.coerceIn(0, 100))
                    }
                }
            }
        } finally {
            conn.disconnect()
        }
    }

    fun deleteModels(modelDir: File): Boolean {
        return try {
            for (file in FILES) {
                val f = File(modelDir, file.name)
                if (f.exists()) f.delete()
            }
            modelDir.delete()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete models: ${e.message}")
            false
        }
    }

    fun verifyModels(modelDir: File): Boolean {
        for (file in FILES) {
            val f = File(modelDir, file.name)
            if (!f.exists() || f.length() == 0L) return false
        }
        return true
    }
}
