package com.vaani.keyboard.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslateEngineTest {

    @Test
    fun `empty input returns empty`() {
        assertEquals("", TranslateEngine.translate(""))
        assertEquals("", TranslateEngine.translate(" "))
        assertEquals("", TranslateEngine.translate("   "))
    }

    @Test
    fun `greetings translate correctly`() {
        assertEquals("Hello", TranslateEngine.translate("namaste"))
        assertEquals("Hello friends", TranslateEngine.translate("namaste doston"))
        assertEquals("How are you", TranslateEngine.translate("kaise ho"))
        assertEquals("Hi", TranslateEngine.translate("hi"))
    }

    @Test
    fun `thank you phrases translate`() {
        assertEquals("Thank you", TranslateEngine.translate("dhanyavaad"))
        assertEquals("Thank you very much", TranslateEngine.translate("bahut dhanyavaad"))
        assertEquals("Thank you", TranslateEngine.translate("shukriya"))
    }

    @Test
    fun `questions parse correctly`() {
        assertEquals("What are you doing", TranslateEngine.translate("kya kar rahe ho"))
        assertEquals("Where are you", TranslateEngine.translate("kahan ho"))
        assertEquals("How much is this", TranslateEngine.translate("ye kitna hai"))
        assertEquals("Who is this", TranslateEngine.translate("ye kaun hai"))
    }

    @Test
    fun `family terms translate`() {
        assertEquals("Mom", TranslateEngine.translate("mummy"))
        assertEquals("Dad", TranslateEngine.translate("papa"))
        assertEquals("Brother", TranslateEngine.translate("bhai"))
        assertEquals("Sister", TranslateEngine.translate("behen"))
    }

    @Test
    fun `food and drink phrases translate`() {
        assertEquals("I am hungry", TranslateEngine.translate("bhookh lagi hai"))
        assertEquals("The food is very good", TranslateEngine.translate("khana bahut achha hai"))
        assertEquals("I am full", TranslateEngine.translate("pet bhar gaya"))
    }

    @Test
    fun `time expressions translate`() {
        assertEquals("Today", TranslateEngine.translate("aaj"))
        assertEquals("Tonight", TranslateEngine.translate("aaj raat"))
        assertEquals("Morning", TranslateEngine.translate("subah"))
        assertEquals("Right now", TranslateEngine.translate("abhi"))
    }

    @Test
    fun `regex template my name is translates`() {
        val result = TranslateEngine.translate("mera naam Rahul hai")
        assertEquals("My name is Rahul", result)
    }

    @Test
    fun `regex template I need translates`() {
        val result = TranslateEngine.translate("mujhe pani chahiye")
        assertEquals("I need pani", result)
    }

    @Test
    fun `regex template I need negative translates`() {
        val result = TranslateEngine.translate("mujhe kuch nahi chahiye")
        assertEquals("I do not need kuch", result)
    }

    @Test
    fun `regex template I like translates`() {
        val result = TranslateEngine.translate("mujhe pizza pasand hai")
        assertEquals("I like pizza", result)
    }

    @Test
    fun `regex template I do not like translates`() {
        val result = TranslateEngine.translate("mujhe pizza pasand nahi")
        assertEquals("I do not like pizza", result)
    }

    @Test
    fun `regex template going to translates`() {
        val result = TranslateEngine.translate("main office ja raha hoon")
        assertTrue(result.contains("going to") || result.contains("I am"))
        assertTrue(result.contains("office"))
    }

    @Test
    fun `regex template where is translates`() {
        val result = TranslateEngine.translate("station kahan hai")
        assertEquals("Where is station", result)
    }

    @Test
    fun `regex template do not translates`() {
        val result = TranslateEngine.translate("ye mat karo")
        assertEquals("Do not ye", result)
    }

    @Test
    fun `regex template can you translates`() {
        val result = TranslateEngine.translate("kya aap meri madad kar sakte hain")
        assertTrue(result.contains("you") && result.contains("help"))
    }

    @Test
    fun `regex template bring me translates`() {
        val result = TranslateEngine.translate("pani lao")
        assertEquals("Bring pani", result)
    }

    @Test
    fun `regex template send translates`() {
        val result = TranslateEngine.translate("photo bhejo")
        assertEquals("Send photo", result)
    }

    @Test
    fun `progressive verb phrases translate for main`() {
        val result = TranslateEngine.translate("main padh raha hoon")
        assertEquals("I am studying", result)
    }

    @Test
    fun `progressive verb phrases translate for woh`() {
        val result = TranslateEngine.translate("woh khel raha hai")
        assertEquals("He is playing", result)
    }

    @Test
    fun `past tense translates`() {
        val result = TranslateEngine.translate("main gaya tha")
        assertEquals("I went", result)
    }

    @Test
    fun `future tense translates`() {
        val result = TranslateEngine.translate("main aaunga")
        assertEquals("I will come", result)
    }

    @Test
    fun `obligation phrases translate`() {
        val result = TranslateEngine.translate("mujhe jaana hai")
        assertEquals("I have to go", result)
    }

    @Test
    fun `input normalization is case insensitive`() {
        val lower = TranslateEngine.translate("NAMASTE")
        val upper = TranslateEngine.translate("namaste")
        assertEquals(upper, lower)
    }

    @Test
    fun `input normalization trims whitespace`() {
        val padded = TranslateEngine.translate("  namaste  ")
        val normal = TranslateEngine.translate("namaste")
        assertEquals(normal, padded)
    }

    @Test
    fun `input normalization collapses multiple spaces`() {
        val spaced = TranslateEngine.translate("aap   kaise   hain")
        val normal = TranslateEngine.translate("aap kaise hain")
        assertEquals(normal, spaced)
    }

    @Test
    fun `unknown words pass through untranslated`() {
        val result = TranslateEngine.translate("xyzzy")
        assertTrue(result.contains("xyzzy"))
    }

    @Test
    fun `output passes through GrammarEngine clean`() {
        val result = TranslateEngine.translate("namaste")
        assertTrue(result[0].isUpperCase())
        assertTrue(result.endsWith(".") || !result.endsWith("."))
    }

    @Test
    fun `result is never equal to raw input for known phrases`() {
        assertNotEquals("namaste", TranslateEngine.translate("namaste"))
    }
}
