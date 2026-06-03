package com.vaani.keyboard.util

object GrammarEngine {

    fun clean(text: String): String {
        if (text.isBlank()) return text

        var result = text.trim()

        result = normalizeWhitespace(result)
        result = expandContractions(result)
        result = fixSubjectVerbAgreement(result)
        result = fixCommonSwaps(result)
        result = fixArticles(result)
        result = fixPrepositions(result)
        result = fixTenseMismatch(result)
        result = fixRedundancy(result)
        result = capitalizeFirst(result)
        result = fixEndPunctuation(result)

        return result
    }

    private fun normalizeWhitespace(text: String): String {
        return text.replace(Regex("\\s+"), " ").trim()
    }

    private val contractions = mapOf(
        "gonna" to "going to", "wanna" to "want to", "gotta" to "got to",
        "kinda" to "kind of", "sorta" to "sort of", "lotsa" to "lots of",
        "coulda" to "could have", "shoulda" to "should have", "woulda" to "would have",
        "musta" to "must have", "mighta" to "might have",
        "u" to "you", "r" to "are", "ur" to "your", "urs" to "yours",
        "urself" to "yourself", "pls" to "please", "plz" to "please",
        "thx" to "thanks", "thanx" to "thanks", "ty" to "thank you",
        "btw" to "by the way", "idk" to "I don't know", "imo" to "in my opinion",
        "imho" to "in my humble opinion", "lol" to "haha", "brb" to "be right back",
        "gtg" to "got to go", "ttyl" to "talk to you later", "omw" to "on my way",
        "nvm" to "never mind", "atm" to "at the moment", "asap" to "as soon as possible",
        "afaik" to "as far as I know", "ikr" to "I know, right",
        "rn" to "right now", "dm" to "direct message",
        "msg" to "message", "pic" to "picture", "pics" to "pictures",
        "cud" to "could", "shud" to "should", "wud" to "would",
    )

    private val contractionPatterns = contractions.map { (short, long) ->
        Regex("\\b$short\\b", RegexOption.IGNORE_CASE) to long
    }

    private fun expandContractions(text: String): String {
        var result = text
        for ((regex, longForm) in contractionPatterns) {
            result = result.replace(regex) {
                if (it.value[0].isUpperCase()) longForm.replaceFirstChar { c -> c.uppercase() }
                else longForm
            }
        }
        return result
    }

    private val subjVerbs = arrayOf(
        arrayOf(Regex("\\bhe don't\\b", RegexOption.IGNORE_CASE), "he doesn't"),
        arrayOf(Regex("\\bshe don't\\b", RegexOption.IGNORE_CASE), "she doesn't"),
        arrayOf(Regex("\\bit don't\\b", RegexOption.IGNORE_CASE), "it doesn't"),
        arrayOf(Regex("\\bhe go\\b", RegexOption.IGNORE_CASE), "he goes"),
        arrayOf(Regex("\\bshe go\\b", RegexOption.IGNORE_CASE), "she goes"),
        arrayOf(Regex("\\bit go\\b", RegexOption.IGNORE_CASE), "it goes"),

        arrayOf(Regex("\\bhe have\\b", RegexOption.IGNORE_CASE), "he has"),
        arrayOf(Regex("\\bshe have\\b", RegexOption.IGNORE_CASE), "she has"),
        arrayOf(Regex("\\bit have\\b", RegexOption.IGNORE_CASE), "it has"),

        arrayOf(Regex("\\bhe do\\b", RegexOption.IGNORE_CASE), "he does"),
        arrayOf(Regex("\\bshe do\\b", RegexOption.IGNORE_CASE), "she does"),
        arrayOf(Regex("\\bit do\\b", RegexOption.IGNORE_CASE), "it does"),

        arrayOf(Regex("\\bthey goes\\b", RegexOption.IGNORE_CASE), "they go"),
        arrayOf(Regex("\\bwe goes\\b", RegexOption.IGNORE_CASE), "we go"),
        arrayOf(Regex("\\byou goes\\b", RegexOption.IGNORE_CASE), "you go"),

        arrayOf(Regex("\\bthey has\\b", RegexOption.IGNORE_CASE), "they have"),
        arrayOf(Regex("\\bwe has\\b", RegexOption.IGNORE_CASE), "we have"),
        arrayOf(Regex("\\byou has\\b", RegexOption.IGNORE_CASE), "you have"),

        arrayOf(Regex("\\bthey does\\b", RegexOption.IGNORE_CASE), "they do"),
        arrayOf(Regex("\\bwe does\\b", RegexOption.IGNORE_CASE), "we do"),
        arrayOf(Regex("\\byou does\\b", RegexOption.IGNORE_CASE), "you do"),

        arrayOf(Regex("\\bi is\\b", RegexOption.IGNORE_CASE), "I am"),
        arrayOf(Regex("\\bi are\\b", RegexOption.IGNORE_CASE), "I am"),
        arrayOf(Regex("\\bi was\\b", RegexOption.IGNORE_CASE), "I was"),

        arrayOf(Regex("\\bhe are\\b", RegexOption.IGNORE_CASE), "he is"),
        arrayOf(Regex("\\bshe are\\b", RegexOption.IGNORE_CASE), "she is"),
        arrayOf(Regex("\\bit are\\b", RegexOption.IGNORE_CASE), "it is"),

        arrayOf(Regex("\\bthey is\\b", RegexOption.IGNORE_CASE), "they are"),
        arrayOf(Regex("\\bwe is\\b", RegexOption.IGNORE_CASE), "we are"),
    )

    private fun fixSubjectVerbAgreement(text: String): String {
        var result = text
        for (rule in subjVerbs) {
            val regex = rule[0] as Regex
            val replacement = rule[1] as String
            result = regex.replace(result, replacement)
        }
        return result
    }

    private val commonSwaps = arrayOf(
        "adviced" to "advised",
        "acheive" to "achieve", "acheived" to "achieved", "acheiving" to "achieving",
        "beleive" to "believe", "beleived" to "believed", "beleiving" to "believing",
        "calender" to "calendar",
        "definately" to "definitely",
        "dissapoint" to "disappoint", "dissapointed" to "disappointed",
        "embarass" to "embarrass", "embarassed" to "embarrassed",
        "famoust" to "famous",
        "goverment" to "government",
        "grammer" to "grammar",
        "happenn" to "happen", "happenned" to "happened",
        "immediatly" to "immediately",
        "jewellery" to "jewelry",
        "knowlege" to "knowledge",
        "langauge" to "language",
        "millon" to "million", "millons" to "millions",
        "neccessary" to "necessary",
        "occassion" to "occasion", "occassional" to "occasional",
        "parlament" to "parliament",
        "recieve" to "receive", "recieved" to "received", "recieving" to "receiving",
        "seperate" to "separate",
        "suprise" to "surprise",
        "tommorow" to "tomorrow", "tommorrow" to "tomorrow", "tomorow" to "tomorrow",
        "untill" to "until",
        "vaccume" to "vacuum",
        "wensday" to "Wednesday",
        "prefered" to "preferred", "refered" to "referred",
        "occured" to "occurred", "occuring" to "occurring",
        "sign up" to "signup", "log in" to "login", "set up" to "setup",
    )

    private val commonSwapPatterns = commonSwaps.map { (wrong, right) ->
        Regex("\\b$wrong\\b", RegexOption.IGNORE_CASE) to right
    }

    private fun fixCommonSwaps(text: String): String {
        var result = text
        for ((regex, right) in commonSwapPatterns) {
            result = result.replace(regex) {
                if (it.value[0].isUpperCase()) right.replaceFirstChar { c -> c.uppercase() }
                else right
            }
        }
        return result
    }

    private val articlePatterns = arrayOf(
        Regex("\\ba\\b\\s+(?=[aeiouAEIOU])") to "an ",
        Regex("\\bthe my\\b", RegexOption.IGNORE_CASE) to "my",
        Regex("\\bthe your\\b", RegexOption.IGNORE_CASE) to "your",
        Regex("\\bthe his\\b", RegexOption.IGNORE_CASE) to "his",
        Regex("\\bthe her\\b", RegexOption.IGNORE_CASE) to "her",
        Regex("\\bthe our\\b", RegexOption.IGNORE_CASE) to "our",
        Regex("\\bthe their\\b", RegexOption.IGNORE_CASE) to "their",
    )

    private fun fixArticles(text: String): String {
        var result = text
        for ((regex, replacement) in articlePatterns) {
            result = regex.replace(result, replacement)
        }
        result = result.replace("a an ", "an ")
        return result
    }

    private val prepositionPatterns = arrayOf(
        Regex("\\bcoupleds of\\b", RegexOption.IGNORE_CASE) to "couple of",
        Regex("\\bmore better\\b", RegexOption.IGNORE_CASE) to "better",
        Regex("\\bmore bigger\\b", RegexOption.IGNORE_CASE) to "bigger",
        Regex("\\bmore smaller\\b", RegexOption.IGNORE_CASE) to "smaller",
    )

    private fun fixPrepositions(text: String): String {
        var result = text
        for ((regex, replacement) in prepositionPatterns) {
            result = regex.replace(result, replacement)
        }
        return result
    }

    private fun fixTenseMismatch(text: String): String {
        var result = text

        val possessionNouns = setOf(
            "car", "bike", "house", "phone", "laptop", "computer", "TV", "tv",
            "book", "pen", "bag", "watch", "bicycle", "vehicle", "motorcycle",
            "smartphone", "tablet", "ipad", "iphone", "android", "macbook",
            "wallet", "key", "keys", "purse", "backpack", "suitcase",
            "apartment", "flat", "room", "property", "land",
        )
        val possessionExclusions = setOf(
            "dinner", "lunch", "breakfast", "brunch", "meal", "food",
            "fun", "time", "party", "meeting", "conversation", "chat",
            "bath", "shower", "swim", "walk", "drive", "ride",
            "class", "lesson", "course", "training", "session",
            "problem", "issue", "difficulty", "trouble",
            "baby", "child", "children",
        )

        result = result.replace(Regex("\\bI am having\\b", RegexOption.IGNORE_CASE)) {
            val hasPossession = possessionNouns.any { noun ->
                Regex("\\b$noun\\b", RegexOption.IGNORE_CASE).containsMatchIn(result.substringAfter("having"))
            }
            val hasExclusion = possessionExclusions.any { exc ->
                Regex("\\b$exc\\b", RegexOption.IGNORE_CASE).containsMatchIn(result.substringAfter("having"))
            }
            if (hasPossession && !hasExclusion) "I have" else it.value
        }

        result = result.replace(Regex("\\bhave you ate\\b", RegexOption.IGNORE_CASE)) { "have you eaten" }
        result = result.replace(Regex("\\bdid you ate\\b", RegexOption.IGNORE_CASE)) { "did you eat" }
        result = result.replace(Regex("\\bdid you went\\b", RegexOption.IGNORE_CASE)) { "did you go" }
        result = result.replace(Regex("\\bdid you did\\b", RegexOption.IGNORE_CASE)) { "did you do" }
        result = result.replace(Regex("\\bdid you came\\b", RegexOption.IGNORE_CASE)) { "did you come" }
        result = result.replace(Regex("\\bdid you took\\b", RegexOption.IGNORE_CASE)) { "did you take" }
        result = result.replace(Regex("\\bdid you gave\\b", RegexOption.IGNORE_CASE)) { "did you give" }

        result = result.replace(Regex("\\bI have been completed\\b", RegexOption.IGNORE_CASE)) { "I have completed" }
        result = result.replace(Regex("\\bI have been finished\\b", RegexOption.IGNORE_CASE)) { "I have finished" }

        result = result.replace(Regex("\\bI am (?:not )?come\\b", RegexOption.IGNORE_CASE)) { "I have come" }
        result = result.replace(Regex("\\bI am (?:not )?went\\b", RegexOption.IGNORE_CASE)) { "I went" }
        result = result.replace(Regex("\\b(?:He|She) is come\\b", RegexOption.IGNORE_CASE)) { "has come" }
        result = result.replace(Regex("\\b(?:He|She) is went\\b", RegexOption.IGNORE_CASE)) { "went" }

        return result
    }

    private val redundancyPatterns = arrayOf(
        Regex("\\brevert back\\b", RegexOption.IGNORE_CASE) to "revert",
        Regex("\\breturn back\\b", RegexOption.IGNORE_CASE) to "return",
        Regex("\\brepeat again\\b", RegexOption.IGNORE_CASE) to "repeat",
        Regex("\\breply back\\b", RegexOption.IGNORE_CASE) to "reply",
        Regex("\\bATM machine\\b", RegexOption.IGNORE_CASE) to "ATM",
        Regex("\\bPIN number\\b", RegexOption.IGNORE_CASE) to "PIN",
        Regex("\\bISBN number\\b", RegexOption.IGNORE_CASE) to "ISBN",
    )

    private fun fixRedundancy(text: String): String {
        var result = text
        for ((regex, replacement) in redundancyPatterns) {
            result = regex.replace(result, replacement)
        }
        return result
    }

    private fun capitalizeFirst(text: String): String {
        if (text.isEmpty()) return text
        return text[0].uppercase() + text.substring(1)
    }

    private val sentenceEnders = setOf('.', '!', '?', ':', ';')

    private fun fixEndPunctuation(text: String): String {
        val trimmed = text.trimEnd()
        if (trimmed.isEmpty()) return text

        val last = trimmed.last()
        if (last in sentenceEnders) return trimmed

        if (trimmed.endsWith("...")) return trimmed

        return "$trimmed."
    }
}
