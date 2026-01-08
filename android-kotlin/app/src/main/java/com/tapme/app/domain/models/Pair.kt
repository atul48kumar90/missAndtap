package com.tapme.app.domain.models

data class Pair(
    val id: String,
    val user1Id: String,
    val user2Id: String?,
    val lastTapAt: String? = null,
    val lastTapAtReverse: String? = null,
    val createdAt: String,
    val streak: Int? = null,
    val lastStreakDate: String? = null
)
