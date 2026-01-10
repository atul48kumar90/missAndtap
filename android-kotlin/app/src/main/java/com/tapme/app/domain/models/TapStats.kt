package com.tapme.app.domain.models

data class TapStats(
    // What you did (sent)
    val todayTapCount: Int,
    val lastTapTime: String?,
    val streak: Int? = null,
    // What you received
    val todayTapsReceived: Int? = null,
    val lastTapReceivedTime: String? = null
)
