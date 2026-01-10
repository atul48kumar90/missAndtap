package com.tapme.app.data.remote

import android.content.Context
import com.tapme.app.BuildConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = BuildConfig.API_BASE_URL
    private const val CONNECT_TIMEOUT = 10L
    private const val READ_TIMEOUT = 10L
    private const val WRITE_TIMEOUT = 10L
    private const val CACHE_SIZE = 10 * 1024 * 1024L // 10MB

    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    /**
     * Custom logger that redacts sensitive information from HTTP logs
     */
    private class SecureLogger : HttpLoggingInterceptor.Logger {
        // Headers that should be redacted
        private val sensitiveHeaders = setOf(
            "authorization",
            "cookie",
            "x-api-key",
            "api-key",
            "x-auth-token",
            "x-access-token"
        )

        override fun log(message: String) {
            // Redact sensitive headers
            var sanitizedMessage = message
            sensitiveHeaders.forEach { headerName ->
                // Match Authorization: Bearer <token> or Authorization: <token>
                val pattern = Regex("$headerName:\\s*(Bearer\\s+)?[^\\r\\n]+", RegexOption.IGNORE_CASE)
                sanitizedMessage = sanitizedMessage.replace(pattern) { matchResult ->
                    val prefix = matchResult.value.substringBefore(":")
                    "$prefix: [REDACTED]"
                }
            }
            
            // Also redact tokens in URL query parameters or response bodies
            sanitizedMessage = sanitizedMessage.replace(
                Regex("token=[^&\\r\\n\\s]+", RegexOption.IGNORE_CASE),
                "token=[REDACTED]"
            )
            sanitizedMessage = sanitizedMessage.replace(
                Regex("\"token\"\\s*:\\s*\"[^\"]+\"", RegexOption.IGNORE_CASE),
                "\"token\": \"[REDACTED]\""
            )
            sanitizedMessage = sanitizedMessage.replace(
                Regex("\"authToken\"\\s*:\\s*\"[^\"]+\"", RegexOption.IGNORE_CASE),
                "\"authToken\": \"[REDACTED]\""
            )
            sanitizedMessage = sanitizedMessage.replace(
                Regex("\"accessToken\"\\s*:\\s*\"[^\"]+\"", RegexOption.IGNORE_CASE),
                "\"accessToken\": \"[REDACTED]\""
            )
            
            android.util.Log.d("OkHttp", sanitizedMessage)
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor(SecureLogger()).apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val cacheInterceptor = CacheInterceptor()

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(cacheInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }

        // Add cache if context is available
        context?.let {
            val cacheDir = File(it.cacheDir, "http-cache")
            val cache = Cache(cacheDir, CACHE_SIZE)
            builder.cache(cache)
        }

        builder.build()
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
