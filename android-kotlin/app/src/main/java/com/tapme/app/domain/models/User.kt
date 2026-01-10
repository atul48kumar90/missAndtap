package com.tapme.app.domain.models

data class User(
    val id: String,
    val deviceId: String,
    val userCode: String? = null,
    val premium: Boolean = false
)
