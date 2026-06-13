package com.vaani.keyboard.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class ModelManager(private val context: Context, private val callback: Callback) {

    interface Callback {
        fun onFileProgress(fileName: String, bytesDownloaded: Long, totalBytes: Long, speedKBps: Long)
        fun onOverallProgress(percent: Int)
        fun onVerify(fileName: String)
        fun onComplete(success: Boolean, message: String)
    }

    companion object {
        private const val TAG = "ModelManager"
        private const val BUFFER_SIZE = 8192
        private const val HASH_ALGORITHM = "SHA-256"
        private const val MAX_RETRIES = 4
        private const val BASE_BACKOFF_MS = 2000L
        private const val MIN_REQUIRED_SPACE = 1_073_741_824L // 1 GB

        private val BASE = "https://huggingface.co/Xenova/nllb-200-distilled-600M/resolve/main/onnx"
        private val SPM_BASE = "https://huggingface.co/facebook/nllb-200-distilled-600M/resolve/main"

        val FILES = listOf(
            ModelFile(
                "encoder_model_quantized.onnx",
                "$BASE/encoder_model_quantized.onnx",
                419_000_000,
                ""
            ),
            ModelFile(
                "decoder_with_past_model_quantized.onnx",
                "$BASE/decoder_with_past_model_quantized.onnx",
                445_000_000,
                ""
            ),
            ModelFile(
                "sentencepiece.bpe.model",
                "$SPM_BASE/sentencepiece.bpe.model",
                4_000_000,
                ""
            )
        )

        data class ModelFile(
            val name: String,
            val url: String,
            val sizeEstimate: Int,
            val expectedSha256: String
        )
    }

    @Volatile
    private var cancelled = false

    fun cancel() {
        cancelled = true
    }

    suspend fun downloadAll(modelDir: File) = withContext(Dispatchers.IO) {
        cancelled = false
        modelDir.mkdirs()

        if (!checkNetwork()) {
            callback.onComplete(false, "No internet connection")
            return@withContext
        }
        if (!hasFreeSpace(modelDir)) {
            callback.onComplete(false, "Insufficient storage space (need ~1 GB)")
            return@withContext
        }

        val totalFiles = FILES.size
        var completedFiles = 0
        val failedFiles = mutableListOf<ModelFile>()

        for (file in FILES) {
            if (cancelled) {
                callback.onComplete(false, "Download cancelled")
                return@withContext
            }
            val target = File(modelDir, file.name)
            if (target.exists() && target.length() > 0) {
                callback.onVerify(file.name)
                if (verifyFileHash(target, file.expectedSha256, file.name)) {
                    completedFiles++
                    updateOverall(completedFiles, totalFiles)
                    continue
                }
                deleteFile(target)
            }
            val success = downloadWithRetry(file, target, completedFiles, totalFiles)
            if (success) {
                completedFiles++
                updateOverall(completedFiles, totalFiles)
            } else {
                failedFiles.add(file)
            }
        }

        if (failedFiles.isEmpty()) {
            callback.onComplete(true, "All model files downloaded successfully")
        } else {
            val names = failedFiles.joinToString(", ") { it.name }
            callback.onComplete(false, "Failed: $names")
        }
    }

    private suspend fun downloadWithRetry(
        file: ModelFile,
        target: File,
        completedFiles: Int,
        totalFiles: Int
    ): Boolean {
        var lastError: String? = null
        for (attempt in 1..MAX_RETRIES) {
            if (cancelled) return false
            if (attempt > 1) {
                val backoff = BASE_BACKOFF_MS * (1 shl (attempt - 2))
                Log.d(TAG, "Retrying ${file.name} in ${backoff}ms (attempt $attempt/$MAX_RETRIES)")
                delay(backoff)
            }
            try {
                downloadFile(file, target, completedFiles, totalFiles)
                callback.onVerify(file.name)
                if (!verifyFileHash(target, file.expectedSha256, file.name)) {
                    deleteFile(target)
                    lastError = "SHA256 mismatch"
                    Log.w(TAG, "$lastError for ${file.name}")
                    continue
                }
                persistSha256(target, file.name)
                return true
            } catch (e: Exception) {
                lastError = e.message ?: "Unknown error"
                Log.e(TAG, "Attempt $attempt/$MAX_RETRIES failed for ${file.name}: $lastError")
                deleteFile(target)
            }
        }
        Log.e(TAG, "All $MAX_RETRIES attempts failed for ${file.name}: $lastError")
        return false
    }

    private fun downloadFile(
        file: ModelFile,
        target: File,
        completedFiles: Int,
        totalFiles: Int
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
                val startTime = System.nanoTime()

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    if (cancelled) {
                        deleteFile(target)
                        return
                    }
                    fos.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead

                    val elapsed = (System.nanoTime() - startTime) / 1_000_000
                    val speedKBps = if (elapsed > 0) (totalBytes * 1000) / (elapsed * 1024) else 0L

                    if (contentLength > 0) {
                        val filePercent = ((totalBytes.toFloat() / contentLength.toFloat()) * 100).toInt()
                        val overall = computeOverall(completedFiles, totalFiles, filePercent)
                        callback.onOverallProgress(overall)
                    }

                    callback.onFileProgress(file.name, totalBytes, contentLength.toLong(), speedKBps)
                }
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun computeOverall(completedFiles: Int, totalFiles: Int, currentFilePercent: Int): Int {
        return ((completedFiles.toFloat() + currentFilePercent.toFloat() / 100f) / totalFiles.toFloat() * 100).toInt()
    }

    private fun updateOverall(completed: Int, total: Int) {
        callback.onOverallProgress((completed.toFloat() / total.toFloat() * 100).toInt())
    }

    private fun checkNetwork(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= 23) {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnected == true
        }
    }

    private fun hasFreeSpace(dir: File): Boolean {
        return dir.freeSpace >= MIN_REQUIRED_SPACE
    }

    fun deleteModels(modelDir: File): Boolean {
        return try {
            for (file in FILES) {
                deleteFile(File(modelDir, file.name))
            }
            modelDir.delete()
            clearHashes()
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
            if (!verifyFileHash(f, file.expectedSha256, file.name)) return false
        }
        return true
    }

    private fun deleteFile(file: File) {
        if (file.exists() && !file.delete()) {
            Log.w(TAG, "Failed to delete ${file.name}")
        }
    }

    private fun verifyFileHash(file: File, expectedHash: String, fileName: String): Boolean {
        val hash = if (expectedHash.isNotBlank()) expectedHash else getStoredHash(fileName)
        if (hash.isNullOrBlank()) return file.exists() && file.length() > 0
        return try {
            val actual = sha256(file)
            actual.equals(hash, ignoreCase = true)
        } catch (e: Exception) {
            Log.w(TAG, "Hash check failed: ${e.message}")
            false
        }
    }

    private fun persistSha256(file: File, name: String) {
        try {
            val hash = sha256(file)
            context.getSharedPreferences("model_hashes", 0).edit()
                .putString("sha256_$name", hash)
                .apply()
            Log.d(TAG, "Stored SHA256 for $name: $hash")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to persist SHA256 for $name: ${e.message}")
        }
    }

    private fun getStoredHash(name: String): String? {
        return try {
            context.getSharedPreferences("model_hashes", 0)
                .getString("sha256_$name", null)
        } catch (_: Exception) { null }
    }

    fun clearHashes() {
        try {
            context.getSharedPreferences("model_hashes", 0).edit().clear().apply()
        } catch (_: Exception) {}
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
