package com.tapme.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.tapme.app.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

/**
 * Manages tap pattern recognition
 * Shows "You usually receive taps around 8 PM" - creates anticipation
 */
class PatternManager private constructor(context: Context) {
    
    private val context = context.applicationContext
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val preferencesManager = PreferencesManager(context)
    
    companion object {
        private const val PREFS_NAME = "pattern_prefs"
        private const val KEY_LAST_PATTERN_CHECK = "last_pattern_check"
        private const val KEY_PATTERN_CACHE = "pattern_cache"
        private const val KEY_IS_TAP_TIME = "is_tap_time"
        private const val KEY_TAP_TIME_MESSAGE = "tap_time_message"
        
        // Cache patterns for 1 hour
        private const val CACHE_DURATION_MS = 60 * 60 * 1000L
        
        @Volatile
        private var INSTANCE: PatternManager? = null
        
        fun getInstance(context: Context): PatternManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PatternManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Get device timezone offset in minutes (JavaScript-style: positive for behind UTC, negative for ahead)
     * Example: EST (UTC-5) = +300, IST (UTC+5:30) = -330
     * This matches JavaScript's Date.getTimezoneOffset() convention
     */
    private fun getTimezoneOffsetMinutes(): Int {
        val timeZone = TimeZone.getDefault()
        val now = System.currentTimeMillis()
        // getOffset() returns milliseconds, positive for ahead of UTC, negative for behind UTC
        // Includes DST automatically if applicable
        val offsetMillis = timeZone.getOffset(now)
        // Convert to minutes and negate to match JavaScript convention
        // JavaScript: positive for behind UTC (EST/UTC-5 = +300)
        // Java: negative for behind UTC (EST = -18000000 ms)
        // So we negate: -(-18000000) / 60000 = +300 ✓
        return -(offsetMillis / (60 * 1000))
    }
    
    /**
     * Check if it's currently "tap time" based on patterns
     * Returns gentle reminder if it's usually tap time (no pressure)
     */
    fun checkTapTime(onResult: (isTapTime: Boolean, message: String?) -> Unit) {
        val token = preferencesManager.getToken()
        if (token == null) {
            onResult(false, null)
            return
        }
        
        // Check cache first
        val cachedIsTapTime = prefs.getBoolean(KEY_IS_TAP_TIME, false)
        val cachedMessage = prefs.getString(KEY_TAP_TIME_MESSAGE, null)
        val lastCheck = prefs.getLong(KEY_LAST_PATTERN_CHECK, 0)
        
        // Use cache if checked within last 30 minutes
        if (System.currentTimeMillis() - lastCheck < 30 * 60 * 1000 && cachedMessage != null) {
            onResult(cachedIsTapTime, cachedMessage)
            return
        }
        
        // Get device timezone offset
        val timezoneOffset = getTimezoneOffsetMinutes()
        
        // Fetch from server
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.checkTapTime("Bearer $token", timezoneOffset)
                if (response.isSuccessful && response.body()?.success == true) {
                    val tapTime = response.body()!!
                    
                    // Cache result
                    prefs.edit()
                        .putBoolean(KEY_IS_TAP_TIME, tapTime.isTapTime)
                        .putString(KEY_TAP_TIME_MESSAGE, tapTime.message)
                        .putLong(KEY_LAST_PATTERN_CHECK, System.currentTimeMillis())
                        .apply()
                    
                    CoroutineScope(Dispatchers.Main).launch {
                        onResult(tapTime.isTapTime, tapTime.message)
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        onResult(false, null)
                    }
                }
            } catch (e: Exception) {
                Log.e("PatternManager", "Error checking tap time", e)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(false, null)
                }
            }
        }
    }
    
    /**
     * Get tap patterns (when user usually receives taps)
     */
    fun getPatterns(onResult: (patterns: com.tapme.app.data.remote.TapPatterns?) -> Unit) {
        val token = preferencesManager.getToken()
        if (token == null) {
            onResult(null)
            return
        }
        
        // Check cache first
        val cachedJson = prefs.getString(KEY_PATTERN_CACHE, null)
        val lastCheck = prefs.getLong(KEY_LAST_PATTERN_CHECK, 0)
        
        // Use cache if checked within cache duration
        if (cachedJson != null && System.currentTimeMillis() - lastCheck < CACHE_DURATION_MS) {
            try {
                // Parse cached JSON (simplified - in production use proper JSON parsing)
                onResult(null) // For now, always fetch fresh
            } catch (e: Exception) {
                // Cache invalid, fetch fresh
            }
        }
        
        // Get device timezone offset
        val timezoneOffset = getTimezoneOffsetMinutes()
        
        // Fetch from server
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getTapPatterns("Bearer $token", timezoneOffset)
                if (response.isSuccessful && response.body()?.success == true) {
                    val patterns = response.body()!!.patterns
                    
                    // Cache result
                    prefs.edit()
                        .putLong(KEY_LAST_PATTERN_CHECK, System.currentTimeMillis())
                        .apply()
                    
                    CoroutineScope(Dispatchers.Main).launch {
                        onResult(patterns)
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        onResult(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("PatternManager", "Error getting patterns", e)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(null)
                }
            }
        }
    }
    
    /**
     * Check if current hour matches usual tap time
     */
    fun isCurrentHourTapTime(usualHour: Int?): Boolean {
        if (usualHour == null) return false
        
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        // Consider it "tap time" if within 1 hour of usual time
        return Math.abs(currentHour - usualHour) <= 1
    }
}
