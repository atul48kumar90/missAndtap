package com.tapme.app.data.remote

import com.tapme.app.domain.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    // Taps
    @POST("taps/send")
    suspend fun sendTap(
        @Header("Authorization") token: String,
        @Body request: SendTapRequest
    ): Response<SendTapResponse>
    
    @GET("taps/stats")
    suspend fun getStats(
        @Header("Authorization") token: String
    ): Response<StatsResponse>
    
    @GET("taps/history")
    suspend fun getTapHistory(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int? = 50,
        @Query("offset") offset: Int? = 0
    ): Response<TapHistoryResponse>
    
    @GET("taps/patterns")
    suspend fun getTapPatterns(
        @Header("Authorization") token: String,
        @Query("timezoneOffset") timezoneOffset: Int? = null
    ): Response<TapPatternsResponse>
    
    @GET("taps/tap-time")
    suspend fun checkTapTime(
        @Header("Authorization") token: String,
        @Query("timezoneOffset") timezoneOffset: Int? = null
    ): Response<TapTimeResponse>
    
    // Whitelist
    @GET("whitelist/my-code")
    suspend fun getMyUserCode(
        @Header("Authorization") token: String
    ): Response<UserCodeResponse>
    
    @POST("whitelist/regenerate-code")
    suspend fun regenerateUserCode(
        @Header("Authorization") token: String
    ): Response<UserCodeResponse>
    
    @GET("whitelist/allowed-tappers")
    suspend fun getAllowedTappers(
        @Header("Authorization") token: String
    ): Response<AllowedTappersResponse>
    
    @POST("whitelist/add-tapper")
    suspend fun addTapper(
        @Header("Authorization") token: String,
        @Body request: AddTapperRequest
    ): Response<AddTapperResponse>
    
    @DELETE("whitelist/remove-tapper/{tapperId}")
    suspend fun removeTapper(
        @Header("Authorization") token: String,
        @Path("tapperId") tapperId: String
    ): Response<RemoveTapperResponse>
}

// Request/Response DTOs
data class RegisterRequest(
    val deviceId: String,
    val fcmToken: String? = null
)

data class AuthResponse(
    val success: Boolean,
    val token: String,
    val user: User
)

data class SendTapRequest(
    val toUserCode: String,
    val tapType: String? = null, // TapType enum name (HAPPY_MISS, SAD_MISS, etc.)
    val customEmoji: String? = null, // Custom emoji (max 50 characters)
    val message: String? = null // Optional message (max 100 characters)
)

data class SendTapResponse(
    val success: Boolean,
    val tap: Tap? = null,
    val rateLimit: RateLimit? = null
)

data class RateLimit(
    val dailyRemaining: Int? = null,
    val cooldownMinutes: Int? = null
)

data class StatsResponse(
    val success: Boolean,
    val stats: TapStats
)

// Whitelist DTOs
data class UserCodeResponse(
    val success: Boolean,
    val userCode: String?,
    val message: String? = null
)

data class AllowedTapper(
    val id: String,
    val tapperUserId: String,
    val tapperUserCode: String,
    val nickname: String? = null,
    val addedAt: String,
    val user: User?
)

data class AllowedTappersResponse(
    val success: Boolean,
    val allowedTappers: List<AllowedTapper>
)

data class AddTapperRequest(
    val userCode: String,
    val nickname: String? = null
)

data class AddTapperResponse(
    val success: Boolean,
    val message: String,
    val allowedTapper: AllowedTapper?
)

data class RemoveTapperResponse(
    val success: Boolean,
    val message: String
)

// Tap History DTOs
data class TapHistoryItem(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val timestamp: String,
    val tapType: String?,
    val customEmoji: String?,
    val message: String?,
    val isSent: Boolean // True if current user sent this tap
)

data class TapHistoryResponse(
    val success: Boolean,
    val taps: List<TapHistoryItem>,
    val total: Int? = null,
    val hasMore: Boolean? = null // Indicates if there are more items to load
)

// Tap Pattern DTOs
data class TapPatternInsight(
    val type: String, // "hour" or "day"
    val message: String,
    val strength: Double,
    val hour: Int? = null,
    val day: Int? = null
)

data class TapPatterns(
    val hasPattern: Boolean,
    val insights: List<TapPatternInsight>? = null,
    val mostCommonHour: Int? = null,
    val mostCommonDay: Int? = null,
    val totalTaps: Int? = null,
    val isTapTime: Boolean? = null,
    val hourStrength: Double? = null,
    val dayStrength: Double? = null,
    val message: String? = null
)

data class TapPatternsResponse(
    val success: Boolean,
    val patterns: TapPatterns
)

data class TapTimeResponse(
    val success: Boolean,
    val isTapTime: Boolean,
    val message: String? = null,
    val usualHour: Int? = null
)
