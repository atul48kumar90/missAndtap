package com.tapme.app.data.remote

import com.tapme.app.domain.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    // Pairs
    @POST("pairs/create")
    suspend fun createPair(@Header("Authorization") token: String): Response<PairResponse>
    
    @POST("pairs/join")
    suspend fun joinPair(
        @Header("Authorization") token: String,
        @Body request: JoinPairRequest
    ): Response<PairResponse>
    
    @GET("pairs/{pairId}")
    suspend fun getPair(
        @Header("Authorization") token: String,
        @Path("pairId") pairId: String
    ): Response<PairResponse>
    
    // Taps
    @POST("taps/send")
    suspend fun sendTap(
        @Header("Authorization") token: String,
        @Body request: SendTapRequest
    ): Response<SendTapResponse>
    
    @GET("taps/stats/{pairId}")
    suspend fun getStats(
        @Header("Authorization") token: String,
        @Path("pairId") pairId: String
    ): Response<StatsResponse>
    
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
    val onesignalPlayerId: String? = null
)

data class AuthResponse(
    val success: Boolean,
    val token: String,
    val user: User
)

data class JoinPairRequest(
    val inviteCode: String
)

data class PairResponse(
    val success: Boolean,
    val pair: Pair? = null,
    val inviteCode: String? = null
)

data class SendTapRequest(
    val pairId: String,
    val tapType: String? = null, // TapType enum name (HAPPY_MISS, SAD_MISS, etc.)
    val customEmoji: String? = null, // Custom emoji (max 10 characters)
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
    val addedAt: String,
    val user: User?
)

data class AllowedTappersResponse(
    val success: Boolean,
    val allowedTappers: List<AllowedTapper>
)

data class AddTapperRequest(
    val userCode: String
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
