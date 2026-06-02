package com.vaani.keyboard.util

object AutoCompleteHelper {

    private val words = listOf(
        "the", "I", "to", "a", "is", "of", "and", "in", "you", "that",
        "it", "for", "on", "are", "with", "have", "be", "this", "not", "but",
        "they", "do", "we", "he", "she", "will", "can", "has", "was", "all",
        "my", "your", "his", "her", "our", "their", "its", "me", "up", "no",
        "go", "get", "know", "like", "see", "want", "come", "think", "take", "make",
        "good", "time", "day", "just", "now", "please", "yes", "ok", "okay", "call",
        "work", "home", "here", "there", "then", "when", "what", "where", "why", "how",
        "who", "which", "need", "tell", "let", "ask", "say", "talk", "give", "send",
        "help", "thanks", "thank", "sorry", "wait", "check", "come", "going", "doing",
        "been", "some", "any", "more", "also", "very", "well", "even", "much", "still",
        "love", "miss", "meet", "see", "hear", "feel", "find", "keep", "put", "set",
        "always", "never", "ever", "every", "each", "first", "last", "next", "new", "old",
        "big", "small", "long", "short", "high", "low", "great", "nice", "fine", "sure",
        "really", "quite", "just", "only", "also", "too", "very", "actually", "pretty", "little",
        "about", "around", "between", "under", "over", "through", "after", "before", "during", "without",
        "because", "so", "if", "though", "while", "until", "since", "as", "than", "then",
        "am", "are", "is", "was", "were", "been", "being", "have", "has", "had",
        "do", "does", "did", "doing", "done", "will", "would", "shall", "should", "can",
        "could", "may", "might", "must", "need", "dare", "ought", "used", "going", "able",
        "yes", "no", "not", "never", "maybe", "perhaps", "sure", "okay", "alright", "fine",
        "hello", "hi", "hey", "dear", "friend", "bro", "sis", "mate", "guys", "everyone",
        "whatsapp", "message", "text", "chat", "group", "photo", "video", "file", "link", "status",
        "today", "tomorrow", "yesterday", "morning", "afternoon", "evening", "night", "week", "month", "year",
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
        "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "first", "second", "third", "last", "next", "other", "another", "many", "few", "several",
        "people", "person", "thing", "way", "part", "place", "number", "world", "life", "hand",
        "man", "woman", "child", "boy", "girl", "guy", "family", "father", "mother", "brother",
        "sister", "son", "daughter", "husband", "wife", "friend", "boss", "teacher", "student", "doctor",
        "problem", "question", "answer", "idea", "plan", "change", "chance", "difference", "experience", "result",
        "water", "food", "time", "money", "price", "cost", "bill", "payment", "free", "cheap",
        "office", "shop", "store", "market", "school", "college", "hospital", "bank", "hotel", "restaurant",
        "happy", "sad", "angry", "tired", "busy", "free", "ready", "late", "early", "fast",
        "best", "better", "good", "bad", "worse", "worst", "easy", "hard", "simple", "difficult",
        "right", "wrong", "true", "false", "real", "fake", "open", "close", "start", "stop",
        "eat", "drink", "sleep", "walk", "run", "sit", "stand", "read", "write", "listen",
        "buy", "sell", "pay", "spend", "save", "keep", "lose", "find", "search", "pick",
        "bring", "take", "give", "get", "receive", "send", "share", "show", "tell", "ask",
        "enjoy", "like", "love", "hate", "prefer", "choose", "decide", "forget", "remember", "learn",
        "understand", "know", "think", "believe", "guess", "hope", "wish", "expect", "suppose", "consider",
        "try", "attempt", "manage", "succeed", "fail", "work", "function", "operate", "run", "use",
        "arrive", "leave", "enter", "reach", "return", "visit", "travel", "move", "stay", "live",
        "talk", "speak", "say", "tell", "discuss", "explain", "describe", "mention", "suggest", "recommend"
    )

    fun suggestions(prefix: String, max: Int = 3): List<String> {
        val lower = prefix.lowercase()
        if (lower.length < 2) return emptyList()

        val matches = words.filter { it.startsWith(lower) && it != lower }

        return matches.take(max)
    }
}
