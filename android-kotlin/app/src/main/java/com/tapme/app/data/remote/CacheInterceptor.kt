package com.tapme.app.data.remote

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class CacheInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Cache GET requests for 60 seconds
        if (request.method == "GET") {
            val cacheControl = CacheControl.Builder()
                .maxAge(60, TimeUnit.SECONDS)
                .build()
            
            return response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .removeHeader("Pragma")
                .build()
        }

        return response
    }
}
