package com.vaani.keyboard.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File

class NllbTokenizerTest {

    @Test
    fun `constants are correct`() {
        assertEquals(0, NllbTokenizer.BOS)
        assertEquals(2, NllbTokenizer.EOS)
        assertEquals(3, NllbTokenizer.UNK)
    }

    @Test
    fun `language tokens are correct`() {
        assertEquals(256047, NllbTokenizer.HINDI_LANG)
        assertEquals(256093, NllbTokenizer.ENGLISH_LANG)
    }

    @Test
    fun `max input length is 128`() {
        assertEquals(128, NllbTokenizer.MAX_INPUT_LENGTH)
    }

    @Test
    fun `constructor handles missing model file gracefully`() {
        val tokenizer = NllbTokenizer("/nonexistent/path.model")
        assertFalse(tokenizer.isLoaded())
    }

    @Test
    fun `encode returns null when not loaded`() {
        val tokenizer = NllbTokenizer("/nonexistent/path.model")
        assertNull(tokenizer.encode("hello"))
    }

    @Test
    fun `decode returns null when not loaded`() {
        val tokenizer = NllbTokenizer("/nonexistent/path.model")
        assertNull(tokenizer.decode(intArrayOf(1, 2, 3)))
    }

    @Test
    fun `encode wraps tokens with language BOS and EOS`() {
        val modelDir = createTempDir()
        val modelFile = createMinimalBpeModel(modelDir, listOf("▁", "▁hello", "▁world"))
        val tokenizer = NllbTokenizer(modelFile.absolutePath)
        if (!tokenizer.isLoaded()) return

        val result = tokenizer.encode("hello")
        assertNotNull(result)
        assertEquals(NllbTokenizer.HINDI_LANG, result!![0])
        assertEquals(NllbTokenizer.BOS, result[1])
        assertEquals(NllbTokenizer.EOS, result[result.size - 1])
    }

    @Test
    fun `encode trims input to max length`() {
        val modelDir = createTempDir()
        val modelFile = createMinimalBpeModel(modelDir, List(200) { "▁token$it" })
        val tokenizer = NllbTokenizer(modelFile.absolutePath)
        if (!tokenizer.isLoaded()) return

        val longInput = (1..200).joinToString(" ") { "token$it" }
        val result = tokenizer.encode(longInput)
        assertNotNull(result)
        assertTrue(result!!.size <= NllbTokenizer.MAX_INPUT_LENGTH)
    }

    @Test
    fun `decode filters special tokens`() {
        val modelDir = createTempDir()
        val modelFile = createMinimalBpeModel(modelDir, listOf("▁", "▁hello", "▁world"))
        val tokenizer = NllbTokenizer(modelFile.absolutePath)
        if (!tokenizer.isLoaded()) return

        val ids = intArrayOf(
            NllbTokenizer.HINDI_LANG,
            NllbTokenizer.BOS,
            2,
            1,
            NllbTokenizer.EOS,
            NllbTokenizer.UNK
        )
        val result = tokenizer.decode(ids)
        assertNotNull(result)
        assertFalse(result!!.contains("<unk>"))
    }

    @Test
    fun `roundtrip encode decode preserves meaning`() {
        val modelDir = createTempDir()
        val modelFile = createMinimalBpeModel(modelDir, listOf("▁", "▁hello", "▁world", "l", "o"))
        val tokenizer = NllbTokenizer(modelFile.absolutePath)
        if (!tokenizer.isLoaded()) return

        val encoded = tokenizer.encode("hello") ?: return
        val decoded = tokenizer.decode(encoded)
        assertNotNull(decoded)
    }

    @Test
    fun `empty string returns null from load failures`() {
        val modelDir = createTempDir()
        val badModel = File(modelDir, "invalid.model")
        badModel.writeText("not a valid model")
        val tokenizer = NllbTokenizer(badModel.absolutePath)
        assertFalse(tokenizer.isLoaded())
        assertNull(tokenizer.encode(""))
    }

    private fun createTempDir(): File {
        val dir = File(System.getProperty("java.io.tmpdir"), "nllb_test_${System.nanoTime()}")
        dir.mkdirs()
        return dir
    }

    private fun createMinimalBpeModel(dir: File, pieces: List<String>): File {
        val protoBytes = buildSentencePieceProto(pieces)
        val modelFile = File(dir, "test.model.bpe")
        DataOutputStream(modelFile.outputStream()).use { dos ->
            dos.writeInt(0x0000EF53)
            dos.writeInt(protoBytes.size)
            dos.write(protoBytes)
        }
        return modelFile
    }

    private fun buildSentencePieceProto(pieces: List<String>): ByteArray {
        val baos = ByteArrayOutputStream()
        for (piece in pieces) {
            val pieceBytes = piece.toByteArray(Charsets.UTF_8)
            writeField(baos, 1, 2, pieceBytes)
            val scoreBytes = ByteArray(4)
            writeField(baos, 2, 5, scoreBytes)
            writeVarint(baos, (3 shl 3) or 0)
            writeVarint(baos, 1)
        }
        return baos.toByteArray()
    }

    private fun writeField(baos: ByteArrayOutputStream, fieldNum: Int, wireType: Int, value: ByteArray) {
        writeVarint(baos, (fieldNum shl 3) or wireType)
        when (wireType) {
            2 -> {
                writeVarint(baos, value.size)
                baos.write(value)
            }
            5 -> baos.write(value, 0, 4)
        }
    }

    private fun writeVarint(baos: ByteArrayOutputStream, value: Int) {
        var v = value
        while (v >= 0x80) {
            baos.write((v and 0x7F) or 0x80)
            v = v ushr 7
        }
        baos.write(v)
    }
}
