package com.tapme.app.domain.models

data class TapStats(
    val todayTapCount: Int,
    val lastTapTime: String?,
    val streak: Int? = null
)
