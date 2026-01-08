package com.tapme.app.domain.models

import android.graphics.Color

enum class TapType(
    val emoji: String,
    val displayName: String,
    val notificationTitle: String,
    val notificationBody: String,
    val vibrationPattern: LongArray,
    val backgroundColor: Int // Background color for full-screen notification
) {
    HAPPY_MISS(
        emoji = "😊",
        displayName = "Happy Miss",
        notificationTitle = "You were missed 😊",
        notificationBody = "Someone is thinking of you with a smile",
        vibrationPattern = longArrayOf(0, 200, 100, 200), // Short, gentle
        backgroundColor = Color.parseColor("#FFD700") // Gold/Yellow - happy color
    ),
    SAD_MISS(
        emoji = "😢",
        displayName = "Sad Miss",
        notificationTitle = "You were missed 😢",
        notificationBody = "Someone is missing you",
        vibrationPattern = longArrayOf(0, 300, 200, 300), // Longer, softer
        backgroundColor = Color.parseColor("#4A90E2") // Soft blue - sad/melancholic color
    ),
    NAUGHTY_MISS(
        emoji = "😏",
        displayName = "Naughty Miss",
        notificationTitle = "You were missed 😏",
        notificationBody = "Someone is thinking of you",
        vibrationPattern = longArrayOf(0, 100, 50, 100, 50, 100), // Quick, playful
        backgroundColor = Color.parseColor("#E91E63") // Hot pink/red - passionate/naughty
    ),
    LOVING_MISS(
        emoji = "❤️",
        displayName = "Loving Miss",
        notificationTitle = "You were missed ❤️",
        notificationBody = "Someone loves and misses you",
        vibrationPattern = longArrayOf(0, 250, 100, 250), // Warm, steady
        backgroundColor = Color.parseColor("#FF69B4") // Pink - romantic/loving
    ),
    EXCITED_MISS(
        emoji = "🤗",
        displayName = "Excited Miss",
        notificationTitle = "You were missed 🤗",
        notificationBody = "Someone can't wait to see you",
        vibrationPattern = longArrayOf(0, 150, 80, 150, 80, 150), // Energetic
        backgroundColor = Color.parseColor("#FFA500") // Orange - energetic/excited
    ),
    SLEEPY_MISS(
        emoji = "😴",
        displayName = "Sleepy Miss",
        notificationTitle = "You were missed 😴",
        notificationBody = "Someone is thinking of you before sleep",
        vibrationPattern = longArrayOf(0, 400), // Long, gentle
        backgroundColor = Color.parseColor("#6B46C1") // Purple - calm/sleepy
    ),
    PLAYFUL_MISS(
        emoji = "😄",
        displayName = "Playful Miss",
        notificationTitle = "You were missed 😄",
        notificationBody = "Someone is playfully missing you",
        vibrationPattern = longArrayOf(0, 100, 50, 100, 50, 100, 50, 100), // Bouncy
        backgroundColor = Color.parseColor("#00CED1") // Turquoise - playful/fun
    ),
    THINKING_MISS(
        emoji = "🤔",
        displayName = "Thinking Miss",
        notificationTitle = "You were missed 🤔",
        notificationBody = "Someone is thinking about you",
        vibrationPattern = longArrayOf(0, 200, 150, 200), // Thoughtful
        backgroundColor = Color.parseColor("#708090") // Slate gray - thoughtful/contemplative
    );

    companion object {
        fun fromString(value: String): TapType? {
            return values().find { it.name == value }
        }
    }
}
