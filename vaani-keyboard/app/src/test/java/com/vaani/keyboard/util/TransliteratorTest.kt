package com.vaani.keyboard.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransliteratorTest {

    @Test
    fun `empty input returns empty`() {
        assertEquals("", Transliterator.transliterate(""))
        assertEquals("", Transliterator.transliterate(" "))
    }

    @Test
    fun `vowels transliterate`() {
        assertEquals("अ", Transliterator.transliterate("a"))
        assertEquals("आ", Transliterator.transliterate("aa"))
        assertEquals("इ", Transliterator.transliterate("i"))
        assertEquals("ई", Transliterator.transliterate("ii"))
        assertEquals("उ", Transliterator.transliterate("u"))
        assertEquals("ए", Transliterator.transliterate("e"))
        assertEquals("ओ", Transliterator.transliterate("o"))
    }

    @Test
    fun `consonant vowel combos transliterate`() {
        assertEquals("क", Transliterator.transliterate("ka"))
        assertEquals("ख", Transliterator.transliterate("kha"))
        assertEquals("ग", Transliterator.transliterate("ga"))
        assertEquals("म", Transliterator.transliterate("ma"))
        assertEquals("न", Transliterator.transliterate("na"))
        assertEquals("र", Transliterator.transliterate("ra"))
    }

    @Test
    fun `matras combine with consonants`() {
        assertEquals("कि", Transliterator.transliterate("ki"))
        assertEquals("की", Transliterator.transliterate("kii"))
        assertEquals("कु", Transliterator.transliterate("ku"))
        assertEquals("के", Transliterator.transliterate("ke"))
        assertEquals("को", Transliterator.transliterate("ko"))
    }

    @Test
    fun `halant produces consonant without vowel`() {
        val k = Transliterator.transliterate("k")
        assertTrue(k.endsWith("्") || k == "क्")
    }

    @Test
    fun `full words transliterate correctly`() {
        assertEquals("नमस्ते", Transliterator.transliterate("namaste"))
        assertEquals("हैलो", Transliterator.transliterate("haelo"))
        assertEquals("पानी", Transliterator.transliterate("paani"))
    }

    @Test
    fun `common phrases transliterate`() {
        assertEquals("आप कैसे हैं", Transliterator.transliterate("aap kaise hain"))
        assertEquals("मैं ठीक हूँ", Transliterator.transliterate("main theek hoon"))
    }

    @Test
    fun `conjuncts approximates`() {
        val result = Transliterator.transliterate("kripya")
        assertTrue(result.contains("कृ") || result.contains("क्"))
    }

    @Test
    fun `numbers pass through unchanged`() {
        assertEquals("123", Transliterator.transliterate("123"))
    }

    @Test
    fun `mixed alphanumeric preserves numbers`() {
        assertEquals("नमस्ते123", Transliterator.transliterate("namaste123"))
    }

    @Test
    fun `unmatched latin characters pass through`() {
        assertEquals("xyz", Transliterator.transliterate("xyz"))
    }

    @Test
    fun `greedy longest match is preferred`() {
        assertEquals("क", Transliterator.transliterate("ka"))
        assertTrue(Transliterator.transliterate("kha").startsWith("ख"))
    }

    @Test
    fun `case insensitive transliteration`() {
        assertEquals(Transliterator.transliterate("KA"), Transliterator.transliterate("ka"))
        assertEquals(Transliterator.transliterate("Namaste"), Transliterator.transliterate("namaste"))
    }
}
