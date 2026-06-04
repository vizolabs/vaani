package com.vaani.keyboard.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GrammarEngineTest {

    @Test
    fun `empty text returns empty`() {
        assertEquals("", GrammarEngine.clean(""))
        assertEquals("", GrammarEngine.clean("   "))
    }

    @Test
    fun `normalizes multiple spaces`() {
        assertEquals("Hello world.", GrammarEngine.clean("Hello   world"))
    }

    @Test
    fun `trims leading and trailing whitespace`() {
        assertEquals("Hello.", GrammarEngine.clean("  Hello  "))
    }

    @Test
    fun `expands common contractions`() {
        assertEquals("I am going to do it.", GrammarEngine.clean("I am gonna do it"))
        assertEquals("I want to go.", GrammarEngine.clean("I wanna go"))
        assertEquals("By the way, hello.", GrammarEngine.clean("btw, hello"))
        assertEquals("I don't know.", GrammarEngine.clean("idk"))
        assertEquals("Be right back.", GrammarEngine.clean("brb"))
        assertEquals("Thanks.", GrammarEngine.clean("thx"))
    }

    @Test
    fun `fixes subject verb agreement`() {
        assertEquals("He doesn't know.", GrammarEngine.clean("He don't know"))
        assertEquals("She has a car.", GrammarEngine.clean("She have a car"))
        assertEquals("They are here.", GrammarEngine.clean("They is here"))
        assertEquals("I am ready.", GrammarEngine.clean("I is ready"))
        assertEquals("He goes there.", GrammarEngine.clean("He go there"))
    }

    @Test
    fun `fixes common spelling swaps`() {
        assertEquals("I definitely agree.", GrammarEngine.clean("I definately agree"))
        assertEquals("Did you receive it.", GrammarEngine.clean("Did you recieve it"))
        assertEquals("See you tomorrow.", GrammarEngine.clean("See you tommorow"))
        assertEquals("I believe you.", GrammarEngine.clean("I beleive you"))
        assertEquals("Is it separate.", GrammarEngine.clean("Is it seperate"))
    }

    @Test
    fun `fixes article a to an before vowels`() {
        assertEquals("An apple a day.", GrammarEngine.clean("A apple a day"))
        assertEquals("An elephant.", GrammarEngine.clean("A elephant"))
        assertEquals("An idea.", GrammarEngine.clean("A idea"))
    }

    @Test
    fun `fixes possessive article patterns`() {
        assertEquals("This is my book.", GrammarEngine.clean("This is the my book"))
        assertEquals("This is your car.", GrammarEngine.clean("This is the your car"))
    }

    @Test
    fun `fixes redundant phrases`() {
        assertEquals("Please revert.", GrammarEngine.clean("Please revert back"))
        assertEquals("Please return.", GrammarEngine.clean("Please return back"))
        assertEquals("Please repeat.", GrammarEngine.clean("Please repeat again"))
    }

    @Test
    fun `fixes tense mismatches`() {
        assertEquals("Have you eaten.", GrammarEngine.clean("Have you ate"))
        assertEquals("Did you eat.", GrammarEngine.clean("Did you ate"))
        assertEquals("Did you go.", GrammarEngine.clean("Did you went"))
        assertEquals("I have completed it.", GrammarEngine.clean("I have been completed it"))
    }

    @Test
    fun `capitalizes first letter`() {
        assertEquals("Hello.", GrammarEngine.clean("hello"))
        assertEquals("Good morning.", GrammarEngine.clean("good morning"))
    }

    @Test
    fun `adds period at end if missing`() {
        val result = GrammarEngine.clean("hello")
        assertTrue(result.endsWith("."))
    }

    @Test
    fun `does not double punctuate`() {
        assertEquals("Hello!", GrammarEngine.clean("Hello!"))
        assertEquals("Hello?", GrammarEngine.clean("Hello?"))
        assertEquals("Hello.", GrammarEngine.clean("Hello."))
    }

    @Test
    fun `full pipeline produces readable English`() {
        val result = GrammarEngine.clean("  he don't know what  to do  ")
        assertEquals("He doesn't know what to do.", result)
    }

    @Test
    fun `preserves trailing ellipsis`() {
        assertEquals("Hello...", GrammarEngine.clean("Hello..."))
        assertTrue(GrammarEngine.clean("Hmm...").endsWith("..."))
    }

    @Test
    fun `handles mixed case contractions`() {
        val result = GrammarEngine.clean("BTW I'm coming")
        assertTrue(result.contains("By the way"))
    }

    @Test
    fun `multiple corrections in cascade`() {
        val result = GrammarEngine.clean("i am gonna revert back the my seperate file")
        assertTrue(result.startsWith("I am"))
        assertTrue(result.contains("going to"))
        assertTrue(result.contains("revert"))
        assertTrue(result.contains("my"))
        assertTrue(result.contains("separate"))
    }
}
