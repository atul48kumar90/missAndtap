package com.tapme.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "taps")
data class TapEntity(
    @PrimaryKey
    val id: String,
    val pairId: String,
    val fromUserId: String,
    val toUserId: String,
    val timestamp: Date,
    val tapType: String? = null,
    val synced: Boolean = false
)
