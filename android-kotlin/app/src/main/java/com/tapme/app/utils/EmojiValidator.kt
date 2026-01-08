package com.tapme.app.utils

object EmojiValidator {
    // Unicode ranges for emojis
    private val emojiPattern = Regex(
        "[\uD83C-\uDBFF\uDC00-\uDFFF]+|" + // Emoticons
        "[\u2600-\u27FF]+|" + // Miscellaneous Symbols
        "[\u2700-\u27BF]+|" + // Dingbats
        "[\uFE00-\uFE0F]+|" + // Variation Selectors
        "[\u203C-\u3299]+|" + // Various Unicode ranges
        "[\u00A9\u00AE\u2122\u2139\u2194-\u2199\u21A9\u21AA\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA\u24C2\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE\u2600-\u2604\u260E\u2611\u2614\u2615\u2618\u261D\u2620\u2622\u2623\u2626\u262A\u262E\u262F\u2638-\u263A\u2640\u2642\u2648-\u2653\u2660\u2663\u2665\u2666\u2668\u267B\u267F\u2692-\u2697\u2699\u269B\u269C\u26A0\u26A1\u26AA\u26AB\u26B0\u26B1\u26BD\u26BE\u26C4\u26C5\u26C8\u26CE\u26CF\u26D1\u26D3\u26D4\u26E9\u26EA\u26F0-\u26F5\u26F7-\u26FA\u26FD\u2702\u2705\u2708-\u270D\u270F\u2712\u2714\u2716\u271D\u2721\u2728\u2733\u2734\u2744\u2747\u274C\u274E\u2753-\u2755\u2757\u2763\u2764\u2795-\u2797\u27A1\u27B0\u27BF\u2934\u2935\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55\u3030\u303D\u3297\u3299]+"
    )

    /**
     * Validates if the input string contains valid emoji characters
     * @param input The string to validate
     * @return true if input contains valid emojis, false otherwise
     */
    fun isValidEmoji(input: String): Boolean {
        if (input.isBlank()) {
            return false
        }
        
        // Check if all characters are emoji
        return input.all { char ->
            emojiPattern.matches(char.toString()) || 
            Character.getType(char) == Character.SURROGATE.toInt() ||
            char.code in 0x1F300..0x1F9FF || // Emoticons
            char.code in 0x2600..0x26FF || // Miscellaneous Symbols
            char.code in 0x2700..0x27BF || // Dingbats
            char.code in 0xFE00..0xFE0F || // Variation Selectors
            char.code in 0x1F900..0x1F9FF || // Supplemental Symbols
            char.code in 0x1F600..0x1F64F || // Emoticons
            char.code in 0x1F300..0x1F5FF || // Miscellaneous Symbols and Pictographs
            char.code in 0x1F680..0x1F6FF || // Transport and Map Symbols
            char.code in 0x1F1E0..0x1F1FF || // Regional Indicator Symbols
            char.code in 0x200D || // Zero Width Joiner
            char.code in 0xFE0F || // Variation Selector-16
            char.code in 0x20E3 // Combining Enclosing Keycap
        }
    }

    /**
     * Counts the number of emoji characters in the input
     * This is approximate as some emojis are multi-character
     */
    fun countEmojis(input: String): Int {
        return input.length // Simple count, can be improved
    }
}
