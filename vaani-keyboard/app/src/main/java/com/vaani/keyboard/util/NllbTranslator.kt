package com.vaani.keyboard.util

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OnnxValue
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File

class NllbTranslator(
    private val encoderPath: String,
    private val decoderPath: String,
    private val tokenizer: NllbTokenizer
) {
    private var ortEnv: OrtEnvironment? = null
    private var encoderSession: OrtSession? = null
    private var decoderSession: OrtSession? = null

    private var encoderInputNames: List<String> = emptyList()
    private var encoderOutputNames: List<String> = emptyList()
    private var decoderInputNames: List<String> = emptyList()
    private var decoderOutputNames: List<String> = emptyList()

    private var kvOutputToInput: Map<String, String> = emptyMap()

    companion object {
        private const val TAG = "NllbTranslator"
        private const val MAX_DECODE_STEPS = 64
        private const val MAX_INPUT_CHARS = 512
        private const val INFERENCE_TIMEOUT_MS = 30_000L
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

            detectTensorNames()
            Log.d(TAG, "Loaded. Encoder ins=$encoderInputNames outs=$encoderOutputNames " +
                    "Decoder ins=$decoderInputNames outs=$decoderOutputNames " +
                    "KV map=$kvOutputToInput")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load models: ${e.message}")
            false
        }
    }

    private fun detectTensorNames() {
        encoderInputNames = (0 until encoderSession!!.getNumInputs())
            .map { encoderSession!!.getInputName(it) }
        encoderOutputNames = (0 until encoderSession!!.getNumOutputs())
            .map { encoderSession!!.getOutputName(it) }
        decoderInputNames = (0 until decoderSession!!.getNumInputs())
            .map { decoderSession!!.getInputName(it) }
        decoderOutputNames = (0 until decoderSession!!.getNumOutputs())
            .map { decoderSession!!.getOutputName(it) }

        kvOutputToInput = buildMap {
            for (outName in decoderOutputNames) {
                if (!outName.startsWith("present.")) continue
                val suffix = outName.removePrefix("present.")
                val expectedInput = "past_key_values.$suffix"
                if (expectedInput in decoderInputNames) {
                    put(outName, expectedInput)
                } else {
                    val match = decoderInputNames.firstOrNull {
                        it == expectedInput || it.endsWith(".$suffix")
                    }
                    if (match != null) put(outName, match)
                }
            }
        }
    }

    fun isLoaded(): Boolean = encoderSession != null && decoderSession != null && tokenizer.isLoaded()

    suspend fun translate(input: String): TranslationResult {
        if (input.isBlank()) return TranslationResult.Error("Empty input")
        if (input.length > MAX_INPUT_CHARS) {
            return TranslationResult.Error("Input too long (max $MAX_INPUT_CHARS chars)")
        }

        return withContext(Dispatchers.IO) {
            try {
                withTimeout(INFERENCE_TIMEOUT_MS) {
                    if (!isLoaded()) return@withTimeout TranslationResult.Error("Model not loaded")
                    val tTotal = Timing(TAG, "translate")
                    var encoderResult: OrtSession.Result? = null
                    var prevDecoderResult: OrtSession.Result? = null
                    try {
                        val tEncode = Timing(TAG, "tokenizer.encode")
                        val inputIds = tokenizer.encode(input)
                            ?: return@withTimeout TranslationResult.Error("Tokenization failed")
                        tEncode.log()

                        val inputIdsLong = inputIds.map { it.toLong() }.toLongArray()
                        val seqLen = inputIds.size.toLong()

                        val encInputs = mutableMapOf<String, OnnxTensor>()
                        encInputs[mapName(encoderInputNames, "input_ids")] =
                            OnnxTensor.createTensor(ortEnv!!, inputIdsLong, longArrayOf(1L, seqLen))
                        encInputs[mapName(encoderInputNames, "attention_mask")] =
                            createAttentionMask(inputIdsLong)

                        val tEncoder = Timing(TAG, "encoder.run")
                        encoderResult = encoderSession!!.run(encInputs)
                        tEncoder.log()
                        encInputs.values.forEach { it.close() }

                        val encoderHiddenName = mapName(encoderOutputNames, "last_hidden_state")
                        val encoderOutput = encoderResult.get(encoderHiddenName) as OnnxTensor
                        var outputIds = intArrayOf(NllbTokenizer.ENGLISH_LANG, NllbTokenizer.BOS)

                        val pastKeyValues = mutableMapOf<String, OnnxValue>()
                        val decoderStepTimes = mutableListOf<Long>()

                        for (step in 0 until MAX_DECODE_STEPS) {
                            val tStep = System.nanoTime()
                            val decoderInput = OnnxTensor.createTensor(
                                ortEnv!!,
                                longArrayOf(outputIds.last().toLong()),
                                longArrayOf(1L, 1L)
                            )
                            val decInputs = buildDecInputs(decoderInput, encoderOutput, inputIdsLong, pastKeyValues)
                            val decoderResult = decoderSession!!.run(decInputs)
                            decoderInput.close()

                            prevDecoderResult?.close()
                            prevDecoderResult = decoderResult

                            val logitsName = mapName(decoderOutputNames, "logits")
                            val logits = decoderResult.get(logitsName) as OnnxTensor
                            val nextId = argMax(logits)

                            pastKeyValues.clear()
                            for ((name, value) in decoderResult) {
                                val inputName = kvOutputToInput[name]
                                if (inputName != null) {
                                    pastKeyValues[inputName] = value
                                }
                            }

                            decoderStepTimes.add((System.nanoTime() - tStep) / 1_000_000)
                            outputIds = outputIds + nextId
                            if (nextId == NllbTokenizer.EOS) break
                        }

                        if (decoderStepTimes.isNotEmpty()) {
                            val avg = decoderStepTimes.average().toLong()
                            val min = decoderStepTimes.min()
                            val max = decoderStepTimes.max()
                            Log.d(TAG, "Decoder steps=${decoderStepTimes.size} avg=${avg}ms min=${min}ms max=${max}ms")
                        }

                        val tDecode = Timing(TAG, "tokenizer.decode")
                        val decoded = tokenizer.decode(outputIds)
                        tDecode.log()

                        tTotal.log()
                        if (decoded != null && decoded.isNotBlank()) {
                            TranslationResult.Success(GrammarEngine.clean(decoded))
                        } else {
                            TranslationResult.Error("Decoding produced empty result")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Translation failed: ${e.message}", e)
                        TranslationResult.Error(e.message ?: "Unknown error")
                    } finally {
                        prevDecoderResult?.close()
                        try { encoderResult?.close() } catch (_: Exception) {}
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Inference timed out after ${INFERENCE_TIMEOUT_MS}ms")
                TranslationResult.Error("Inference timed out")
            }
        }
    }

    private class Timing(private val tag: String, private val label: String) {
        private val start = System.nanoTime()
        fun log() {
            val elapsed = (System.nanoTime() - start) / 1_000_000
            Log.d(tag, "TIME [$label] ${elapsed}ms")
        }
    }

    private fun buildDecInputs(
        decoderInput: OnnxTensor,
        encoderOutput: OnnxTensor,
        inputIdsLong: LongArray,
        pastKeyValues: Map<String, OnnxValue>
    ): Map<String, OnnxValue> {
        val inputs = mutableMapOf<String, OnnxValue>()
        for (name in decoderInputNames) {
            inputs[name] = when {
                name == "input_ids" -> decoderInput
                name == "encoder_hidden_states" || name == "hidden_states" -> encoderOutput
                name.contains("attention_mask") -> createAttentionMask(inputIdsLong)
                pastKeyValues.containsKey(name) -> pastKeyValues[name]!!
                else -> continue
            }
        }
        return inputs
    }

    private fun mapName(names: List<String>, vararg candidates: String): String {
        for (c in candidates) {
            val exact = names.firstOrNull { it == c }
            if (exact != null) return exact
        }
        for (c in candidates) {
            val fuzzy = names.firstOrNull { it.contains(c, ignoreCase = true) }
            if (fuzzy != null) return fuzzy
        }
        return names.first()
    }

    private fun createAttentionMask(idsLong: LongArray): OnnxTensor {
        val mask = idsLong.map { if (it == 0L) 0L else 1L }.toLongArray()
        return OnnxTensor.createTensor(ortEnv!!, mask, longArrayOf(1L, idsLong.size.toLong()))
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
