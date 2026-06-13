package com.vaani.keyboard.util

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class NllbTranslatorTest {

    private val modelDir = File("build/models")
    private val encoderFile = File(modelDir, "encoder_model_quantized.onnx")
    private val decoderFile = File(modelDir, "decoder_with_past_model_quantized.onnx")
    private val tokenizerFile = File(modelDir, "sentencepiece.bpe.model")

    @Before
    fun setUp() {
        assumeTrue(
            "Model files not found at ${modelDir.absolutePath}. " +
                    "Download them first with: python scripts/prepare_model.py build/models",
            modelDir.exists() && encoderFile.exists() && decoderFile.exists() && tokenizerFile.exists()
        )
    }

    @Test
    fun `tokenizer loads successfully`() {
        val tokenizer = NllbTokenizer(tokenizerFile.absolutePath)
        assertTrue("Tokenizer should load", tokenizer.isLoaded())
    }

    @Test
    fun `tokenizer encodes Hinglish input with language prefix`() {
        val tokenizer = NllbTokenizer(tokenizerFile.absolutePath)
        val ids = tokenizer.encode("namaste")
        assertNotNull("encode should return non-null", ids)
        assertEquals(
            "First token should be Hindi lang ID",
            NllbTokenizer.HINDI_LANG,
            ids!![0]
        )
        assertEquals(
            "Second token should be BOS",
            NllbTokenizer.BOS,
            ids[1]
        )
        assertTrue(
            "Last token should be EOS",
            ids.last() == NllbTokenizer.EOS
        )
    }

    @Test
    fun `tokenizer encodes empty input gracefully`() {
        val tokenizer = NllbTokenizer(tokenizerFile.absolutePath)
        val ids = tokenizer.encode("")
        assertNull("Empty input should return null", ids)
    }

    @Test
    fun `tokenizer decode returns text`() {
        val tokenizer = NllbTokenizer(tokenizerFile.absolutePath)
        val sample = intArrayOf(
            NllbTokenizer.ENGLISH_LANG,
            NllbTokenizer.BOS,
            452, 5631, 712, 1314, 7,
            NllbTokenizer.EOS
        )
        val decoded = tokenizer.decode(sample)
        assertNotNull("decode should produce text", decoded)
        assertTrue("Decoded text should not be blank", decoded!!.isNotBlank())
    }

    @Test
    fun `translator loads and translates simple input`() = runTest {
        val tokenizer = NllbTokenizer(tokenizerFile.absolutePath)
        assumeTrue("Tokenizer must load for this test", tokenizer.isLoaded())

        val translator = NllbTranslator(
            encoderFile.absolutePath,
            decoderFile.absolutePath,
            tokenizer
        )
        val loaded = translator.load()
        assertTrue("Translator should load", loaded)

        val result = translator.translate("namaste")
        assertTrue(
            "Translation should succeed: $result",
            result is TranslationResult.Success
        )
        if (result is TranslationResult.Success) {
            assertTrue(
                "Translation should contain greeting: ${result.text}",
                result.text.contains("Hello", ignoreCase = true) ||
                        result.text.contains("hi", ignoreCase = true)
            )
        }
    }

    @Test
    fun `translator handles unknown gracefully`() = runTest {
        val tokenizer = NllbTokenizer(tokenizerFile.absolutePath)
        assumeTrue("Tokenizer must load for this test", tokenizer.isLoaded())

        val translator = NllbTranslator(
            encoderFile.absolutePath,
            decoderFile.absolutePath,
            tokenizer
        )
        val loaded = translator.load()
        assumeTrue("Translator must load for this test", loaded)

        val result = translator.translate("")
        assertTrue("Empty input should return Error", result is TranslationResult.Error)
    }
}
