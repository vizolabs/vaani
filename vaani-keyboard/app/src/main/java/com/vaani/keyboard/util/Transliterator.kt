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
        put("ri", "ऋ"); put("r̥", "ऋ"); put("ru", "ॠ")

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
        put("shra", "श्र"); put("dnya", "ज्ञ"); put("dwa", "द्व")
        put("tta", "त्त"); put("hna", "ह्न")
        put("hya", "ह्य"); put("hva", "ह्व"); put("mna", "म्न")

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
        put("kripya", "कृपया"); put("kripaya", "कृपया")
        put("namaste", "नमस्ते"); put("swagat", "स्वागत")
        put("dhanyavaad", "धन्यवाद"); put("sukriya", "शुक्रिया")
        put("prayas", "प्रयास"); put("pranam", "प्रणाम")
        put("prarthna", "प्रार्थना"); put("sambandh", "सम्बन्ध")
        put("vidya", "विद्या"); put("vidyalay", "विद्यालय")
        put("pradhan", "प्रधान"); put("prakash", "प्रकाश")
        put("sanskrit", "संस्कृत"); put("bharat", "भारत")
        put("trupti", "तृप्ति"); put("kritagya", "कृतज्ञ")
    }

    fun transliterate(input: String): String {
        if (input.isBlank()) return ""

        val words = input.split(" ").filter { it.isNotBlank() }
        return words.joinToString(" ") { word ->
            lookup[word.lowercase()] ?: phoneticFallback(word)
        }
    }

    private val matraMap = mapOf(
        "a" to "ा", "aa" to "ा", "A" to "ा",
        "i" to "ि", "ii" to "ी", "I" to "ी",
        "u" to "ु", "uu" to "ू", "U" to "ू",
        "e" to "े", "E" to "े",
        "o" to "ो", "O" to "ो",
        "ai" to "ै", "au" to "ौ",
        "ri" to "ृ", "r̥" to "ृ",
    )

    private fun phoneticFallback(input: String): String {
        val lower = input.lowercase()
        if (lower.length <= 2) return input

        val result = StringBuilder()
        var i = 0
        while (i < lower.length) {
            val maxLen = minOf(3, lower.length - i)
            var matched = false
            for (len in maxLen downTo 1) {
                val key = lower.substring(i, i + len)
                val mapped = lookup[key]
                if (mapped != null) {
                    if (mapped.endsWith("्") && i + len < lower.length) {
                        val afterKey = lower.substring(i + len)
                        val matraMatch = (minOf(2, afterKey.length) downTo 1)
                            .firstOrNull { mlen ->
                                val mk = afterKey.substring(0, mlen)
                                mk in matraMap
                            }
                        if (matraMatch != null) {
                            val mk = afterKey.substring(0, matraMatch)
                            result.append(mapped.dropLast(1))
                            if (mk != "a") {
                                result.append(matraMap[mk]!!)
                            }
                            i += len + matraMatch
                            matched = true
                            break
                        }
                    }
                    if (!matched) {
                        if (mapped.endsWith("्") && i + len >= lower.length) {
                            result.append(mapped.dropLast(1))
                        } else {
                            result.append(mapped)
                        }
                        i += len
                        matched = true
                        break
                    }
                }
            }
            if (!matched) {
                result.append(lower[i])
                i += 1
            }
        }
        return result.toString()
    }
}
