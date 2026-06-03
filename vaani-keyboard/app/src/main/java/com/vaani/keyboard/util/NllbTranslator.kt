package com.vaani.keyboard.util

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class NllbTranslator(
    private val encoderPath: String,
    private val decoderPath: String,
    private val tokenizer: NllbTokenizer
) {
    private var ortEnv: OrtEnvironment? = null
    private var encoderSession: OrtSession? = null
    private var decoderSession: OrtSession? = null

    companion object {
        private const val TAG = "NllbTranslator"
        private const val MAX_DECODE_STEPS = 64
        private const val BEAM_SIZE = 1
    }

    suspend fun load(): Boolean = withContext(Dispatchers.IO) {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            val encoderFile = File(encoderPath)
            val decoderFile = File(decoderPath)
            if (!encoderFile.exists() || !decoderFile.exists()) {
                Log.e(TAG, "Model files not found")
                return@withContext false
            }
            val ortOpts = OrtSession.SessionOptions()
            encoderSession = ortEnv!!.createSession(encoderPath, ortOpts)
            decoderSession = ortEnv!!.createSession(decoderPath, ortOpts)
            Log.d(TAG, "Models loaded successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load models: ${e.message}")
            false
        }
    }

    fun isLoaded(): Boolean = encoderSession != null && decoderSession != null && tokenizer.isLoaded()

    suspend fun translate(input: String): TranslationResult {
        if (!isLoaded()) return TranslationResult.Error("Model not loaded")
        if (input.isBlank()) return TranslationResult.Error("Empty input")

        return withContext(Dispatchers.IO) {
            try {
                val inputIds = tokenizer.encode(input) ?: return@withContext TranslationResult.Error("Tokenization failed")

                val encoderInput = OnnxTensor.createTensor(ortEnv!!, inputIds)
                val encoderResult = encoderSession!!.run(
                    mapOf("input_ids" to encoderInput, "attention_mask" to createAttentionMask(inputIds))
                )
                encoderInput.close()

                val encoderOutput = encoderResult.get("last_hidden_state") as OnnxTensor
                var outputIds = intArrayOf(NllbTokenizer.ENGLISH_LANG, NllbTokenizer.BOS)

                val pastKeyValues = mutableMapOf<String, OnnxValue>()

                for (step in 0 until MAX_DECODE_STEPS) {
                    val decoderInput = OnnxTensor.createTensor(ortEnv!!, outputIds.last())

                    val decoderInputs = mutableMapOf(
                        "input_ids" to decoderInput,
                        "encoder_hidden_states" to encoderOutput,
                        "encoder_attention_mask" to createAttentionMask(inputIds)
                    )
                    pastKeyValues.forEach { (k, v) -> decoderInputs["past_key_values.${k}"] = v }

                    val decoderResult = decoderSession!!.run(decoderInputs)
                    decoderInput.close()

                    val logits = decoderResult.get("logits") as OnnxTensor
                    val nextId = argMax(logits)
                    logits.close()

                    pastKeyValues.clear()
                    for ((key, value) in decoderResult) {
                        if (key.startsWith("present.")) {
                            val pkvKey = key.removePrefix("present.")
                            pastKeyValues[pkvKey] = value
                        }
                    }

                    outputIds = outputIds + nextId

                    if (nextId == NllbTokenizer.EOS) break
                }

                encoderOutput.close()
                encoderResult.close()

                val decoded = tokenizer.decode(outputIds)
                if (decoded != null && decoded.isNotBlank()) {
                    val cleaned = GrammarEngine.clean(decoded)
                    TranslationResult.Success(cleaned)
                } else {
                    TranslationResult.Error("Decoding produced empty result")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Translation failed: ${e.message}", e)
                TranslationResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun createAttentionMask(ids: IntArray): OnnxTensor {
        val mask = ids.map { if (it == 0) 0L else 1L }.toLongArray()
        return OnnxTensor.createTensor(ortEnv!!, mask)
    }

    private fun argMax(tensor: OnnxTensor): Int {
        val data = tensor.floatBuffer
        var maxIdx = 0
        var maxVal = Float.NEGATIVE_INFINITY
        for (i in 0 until data.limit()) {
            val v = data.get(i)
            if (v > maxVal) {
                maxVal = v
                maxIdx = i
            }
        }
        return maxIdx
    }

    fun close() {
        try {
            encoderSession?.close()
            decoderSession?.close()
        } catch (_: Exception) {}
    }
}
