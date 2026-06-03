package com.vaani.keyboard.util

object Transliterator {

    private val lookup: Map<String, String> = buildMap {
        // Vowels
        put("a", "अ"); put("aa", "आ"); put("A", "आ")
        put("i", "इ"); put("ii", "ई"); put("I", "ई")
        put("u", "उ"); put("uu", "ऊ"); put("U", "ऊ")
        put("e", "ए"); put("E", "ए")
        put("o", "ओ"); put("O", "ओ")
        put("ai", "ऐ"); put("au", "औ")

        // Consonants - ka family
        put("ka", "क"); put("kha", "ख"); put("ga", "ग"); put("gha", "घ"); put("nga", "ङ")

        // cha family
        put("cha", "च"); put("chha", "छ"); put("ja", "ज"); put("jha", "झ"); put("nya", "ञ")

        // ta family (retroflex)
        put("Ta", "ट"); put("Tha", "ठ"); put("Da", "ड"); put("Dha", "ढ"); put("Na", "ण")

        // ta family (dental)
        put("ta", "त"); put("tha", "थ"); put("da", "द"); put("dha", "ध"); put("na", "न")

        // pa family
        put("pa", "प"); put("pha", "फ"); put("ba", "ब"); put("bha", "भ"); put("ma", "म")

        // ya family
        put("ya", "य"); put("ra", "र"); put("la", "ल"); put("va", "व")

        // sibilants
        put("sha", "श"); put("Sha", "ष"); put("sa", "स")

        // Others
        put("ha", "ह"); put("daa", "ड़"); put("Dhaa", "ढ़")

        // Half-letters (for conjuncts) - just vowel removal markers
        put("k", "क्"); put("kh", "ख्"); put("g", "ग्"); put("gh", "घ्")
        put("ch", "च्"); put("chh", "छ्"); put("j", "ज्"); put("jh", "झ्")
        put("T", "ट्"); put("Th", "ठ्"); put("D", "ड्"); put("Dh", "ढ्"); put("N", "ण्")
        put("t", "त्"); put("th", "थ्"); put("d", "द्"); put("dh", "ध्"); put("n", "न्")
        put("p", "प्"); put("ph", "फ्"); put("b", "ब्"); put("bh", "भ्"); put("m", "म्")
        put("y", "य्"); put("r", "र्"); put("l", "ल्"); put("v", "व्")
        put("sh", "श्"); put("S", "ष्"); put("s", "स्"); put("h", "ह्")

        // Special conjuncts
        put("ksha", "क्ष"); put("tra", "त्र"); put("gya", "ज्ञ")
        put("shra", "श्र")

        // Matras (vowel signs after consonant — must not shadow vowel-only keys above)
        put("ee", "ी"); put("oo", "ू")

        // Common Hindi words
        put("namaste", "नमस्ते"); put("dhanyavaad", "धन्यवाद")
        put("kaise", "कैसे"); put("kya", "क्या"); put("kaun", "कौन")
        put("mera", "मेरा"); put("tere", "तेरे"); put("uska", "उसका")
        put("hai", "है"); put("hain", "हैं"); put("ho", "हो")
        put("hum", "हम"); put("aap", "आप"); put("tum", "तुम")
        put("main", "मैं"); put("mein", "में"); put("se", "से")
        put("ko", "को"); put("ka", "का"); put("ki", "की"); put("ke", "के")
        put("aur", "और"); put("yah", "यह"); put("woh", "वह"); put("yeh", "ये")
        put("ve", "वे"); put("aapka", "आपका"); put("aapki", "आपकी")
        put("acha", "अच्छा"); put("theek", "ठीक"); put("sahi", "सही")
        put("nahi", "नहीं"); put("haan", "हाँ"); put("ji", "जी")
        put("chalo", "चलो"); put("dekho", "देखो"); put("karo", "करो")
        put("samajh", "समझ"); put("baat", "बात"); put("kaam", "काम")
        put("ghar", "घर"); put("paani", "पानी"); put("khana", "खाना")
        put("pani", "पानी"); put("kaha", "कहाँ"); put("kab", "कब")
        put("bahut", "बहुत"); put("thoda", "थोड़ा"); put("zyada", "ज़्यादा")
        put("accha", "अच्छा"); put("sundar", "सुंदर")
    }

    fun transliterate(input: String): String {
        if (input.isBlank()) return ""

        val words = input.split(" ").filter { it.isNotBlank() }
        return words.joinToString(" ") { word ->
            lookup[word.lowercase()] ?: phoneticFallback(word)
        }
    }

    private fun phoneticFallback(input: String): String {
        val lower = input.lowercase()
        if (lower.length <= 2) return input

        val result = StringBuilder()
        var i = 0
        while (i < lower.length) {
            // Try longest match first (3 chars, then 2, then 1)
            when {
                i + 3 <= lower.length -> {
                    val triple = lower.substring(i, i + 3)
                    val mapped = lookup[triple]
                    if (mapped != null) {
                        result.append(mapped)
                        i += 3
                    } else {
                        i += 1
                        result.append(lower[i - 1])
                    }
                }
                i + 2 <= lower.length -> {
                    val pair = lower.substring(i, i + 2)
                    val mapped = lookup[pair]
                    if (mapped != null) {
                        result.append(mapped)
                        i += 2
                    } else {
                        i += 1
                        result.append(lower[i - 1])
                    }
                }
                else -> {
                    result.append(lower[i])
                    i += 1
                }
            }
        }
        return result.toString()
    }
}
