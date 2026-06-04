package com.vaani.keyboard.util

import ai.djl.sentencepiece.SpTokenizer
import android.util.Log
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream

class NllbTokenizer(private val modelPath: String) {

    private val processor: SpTokenizer?
    private val fallback: KotlinSentencePiece?

    init {
        var djl: SpTokenizer? = null
        var fb: KotlinSentencePiece? = null
        val modelFile = File(modelPath)

        if (modelFile.exists()) {
            djl = try {
                SpTokenizer.getProcessor(modelFile)
            } catch (e: Exception) {
                Log.w(TAG, "DJL SentencePiece failed: ${e.message}. Trying fallback.")
                null
            }
            if (djl == null) {
                fb = try {
                    KotlinSentencePiece(modelFile)
                } catch (e: Exception) {
                    Log.e(TAG, "Fallback tokenizer also failed: ${e.message}")
                    null
                }
            }
        }
        processor = djl
        fallback = fb
    }

    companion object {
        private const val TAG = "NllbTokenizer"
        const val BOS = 0
        const val EOS = 2
        const val UNK = 3

        const val HINDI_LANG = 256047
        const val ENGLISH_LANG = 256093

        const val MAX_INPUT_LENGTH = 128
    }

    fun isLoaded(): Boolean = processor != null || fallback != null

    fun encode(source: String): IntArray? {
        return try {
            val tokens = if (processor != null) {
                processor!!.encode(source)
            } else if (fallback != null) {
                fallback!!.encode(source)
            } else {
                return null
            }
            val trimmed = tokens.take(MAX_INPUT_LENGTH - 3).toIntArray()
            intArrayOf(HINDI_LANG, BOS) + trimmed + intArrayOf(EOS)
        } catch (e: Exception) {
            Log.e(TAG, "encode failed: ${e.message}")
            null
        }
    }

    fun decode(tokenIds: IntArray): String? {
        return try {
            val filtered = tokenIds.filter { it !in setOf(BOS, EOS, HINDI_LANG, ENGLISH_LANG, UNK) }.toIntArray()
            val decoded = if (processor != null) {
                processor!!.decode(filtered)
            } else if (fallback != null) {
                fallback!!.decode(filtered)
            } else {
                return null
            }
            decoded?.trim()?.replace("<unk>", "")?.trim()
        } catch (e: Exception) {
            Log.e(TAG, "decode failed: ${e.message}")
            null
        }
    }
}

private class KotlinSentencePiece(modelFile: File) {

    private val pieces = mutableListOf<String>()

    init {
        DataInputStream(FileInputStream(modelFile)).use { dis ->
            val magic = dis.readInt()
            if (magic != 0xEF53) {
                throw IllegalArgumentException("Not a valid SentencePiece model")
            }
            val protoLen = dis.readInt()
            val data = ByteArray(protoLen)
            dis.readFully(data)
            parsePieces(data)
        }
        Log.d("KotlinSentencePiece", "Loaded ${pieces.size} pieces")
    }

    private fun parsePieces(data: ByteArray) {
        var i = 0
        while (i < data.size) {
            val tag = data[i].toInt() and 0xFF
            val wireType = tag and 0x07
            val fieldNum = tag ushr 3
            i++

            if (fieldNum == 1 && wireType == 2) {
                val (msgLen, bytesRead) = readVarint(data, i)
                i += bytesRead
                val msg = data.copyOfRange(i, i + msgLen)
                val pieceStr = extractPieceFromSubMessage(msg)
                if (pieceStr != null) pieces.add(pieceStr)
                i += msgLen
            } else {
                i = skipField(data, i, wireType)
                if (i < 0) break
            }
        }
    }

    private fun extractPieceFromSubMessage(msg: ByteArray): String? {
        var j = 0
        while (j < msg.size) {
            val tag = msg[j].toInt() and 0xFF
            val wireType = tag and 0x07
            val fieldNum = tag ushr 3
            j++

            if (fieldNum == 1 && wireType == 2) {
                val (strLen, bytesRead) = readVarint(msg, j)
                j += bytesRead
                val str = String(msg, j, strLen, Charsets.UTF_8)
                return str
            } else {
                j = skipField(msg, j, wireType)
                if (j < 0) break
            }
        }
        return null
    }

    private fun readVarint(data: ByteArray, offset: Int): Pair<Int, Int> {
        var value = 0
        var shift = 0
        var i = offset
        while (i < data.size) {
            val b = data[i].toInt() and 0xFF
            value = value or ((b and 0x7F) shl shift)
            shift += 7
            i++
            if ((b and 0x80) == 0) break
        }
        return Pair(value, i - offset)
    }

    private fun skipField(data: ByteArray, offset: Int, wireType: Int): Int {
        var i = offset
        return when (wireType) {
            0 -> {
                while (i < data.size && (data[i].toInt() and 0x80) != 0) i++
                i + 1
            }
            1 -> i + 8
            2 -> {
                if (i >= data.size) return -1
                val (len, br) = readVarint(data, i)
                i + br + len
            }
            5 -> i + 4
            else -> -1
        }
    }

    fun encode(text: String): List<Int> {
        val tokens = mutableListOf<Int>()
        var pos = 0
        val input = text.trim()
        while (pos < input.length) {
            var bestLen = 0
            var bestId = UNK_FALLBACK
            for (i in pieces.indices) {
                val raw = pieces[i]
                val piece = raw.replace("▁", " ").trim()
                if (piece.isEmpty()) continue
                if (input.regionMatches(pos, piece, 0, piece.length, ignoreCase = true)) {
                    if (piece.length > bestLen) {
                        bestLen = piece.length
                        bestId = i
                    }
                }
            }
            if (bestLen > 0) {
                tokens.add(bestId)
                pos += bestLen
            } else {
                tokens.add(UNK_FALLBACK)
                pos++
            }
        }
        return tokens
    }

    fun decode(ids: IntArray): String {
        val sb = StringBuilder()
        for (id in ids) {
            if (id >= 0 && id < pieces.size) {
                sb.append(pieces[id].replace("▁", " "))
            }
        }
        return sb.toString().trim().replace(Regex("\\s+"), " ")
    }

    companion object {
        private const val UNK_FALLBACK = 3
    }
}
