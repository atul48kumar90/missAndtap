package com.tapme.app.domain.models

data class Tap(
    val id: String,
    val pairId: String,
    val fromUserId: String,
    val toUserId: String,
    val timestamp: String,
    val tapType: String? = null, // TapType enum name
    val synced: Boolean = true
)
