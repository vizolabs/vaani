package com.vaani.keyboard.util

import ai.djl.sentencepiece.SpTokenizer
import java.io.File

class NllbTokenizer(private val modelPath: String) {

    private val processor: SpTokenizer? = try {
        SpTokenizer.getProcessor(File(modelPath))
    } catch (e: Exception) {
        null
    }

    companion object {
        const val BOS = 0
        const val EOS = 2
        const val UNK = 3

        const val HINDI_LANG = 256047
        const val ENGLISH_LANG = 256093

        const val MAX_INPUT_LENGTH = 128
        const val MAX_OUTPUT_LENGTH = 128
    }

    fun isLoaded(): Boolean = processor != null

    fun encode(source: String): IntArray? {
        if (processor == null) return null
        return try {
            val tokens = processor!!.encode(source)
            val trimmed = tokens.take(MAX_INPUT_LENGTH - 3).toIntArray()
            intArrayOf(HINDI_LANG, BOS) + trimmed + intArrayOf(EOS)
        } catch (e: Exception) {
            null
        }
    }

    fun decode(tokenIds: IntArray): String? {
        if (processor == null) return null
        return try {
            val filtered = tokenIds.filter { it !in setOf(BOS, EOS, HINDI_LANG, ENGLISH_LANG, UNK) }.toIntArray()
            val decoded = processor!!.decode(filtered)
            decoded?.trim()?.replace("<unk>", "")?.trim()
        } catch (e: Exception) {
            null
        }
    }
}
