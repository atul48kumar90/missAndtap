package com.tapme.app.utils

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

/**
 * Manages app icon badge count (unread taps)
 * Creates FOMO - users see badge and want to check app
 */
class BadgeManager private constructor(context: Context) {
    
    private val context = context.applicationContext
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "badge_prefs"
        private const val KEY_UNREAD_COUNT = "unread_tap_count"
        
        @Volatile
        private var INSTANCE: BadgeManager? = null
        
        fun getInstance(context: Context): BadgeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BadgeManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Increment unread tap count (when tap received)
     */
    fun incrementUnreadCount() {
        val current = getUnreadCount()
        prefs.edit().putInt(KEY_UNREAD_COUNT, current + 1).apply()
        updateBadge()
    }
    
    /**
     * Clear unread count (when app opened)
     */
    fun clearUnreadCount() {
        prefs.edit().putInt(KEY_UNREAD_COUNT, 0).apply()
        updateBadge()
    }
    
    /**
     * Get current unread count
     */
    fun getUnreadCount(): Int {
        return prefs.getInt(KEY_UNREAD_COUNT, 0)
    }
    
    /**
     * Update app icon badge
     * Note: Badge support varies by device manufacturer
     */
    private fun updateBadge() {
        val count = getUnreadCount()
        
        // For Android 8.0+, use notification badge
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "tap_channel"
            
            // Create a silent notification just for badge count
            if (count > 0) {
                // Badge will show on notification channel
                // Actual notifications will also contribute to badge
            } else {
                // Clear badge by canceling any badge-only notifications
            }
        }
        
        // For Samsung, Xiaomi, etc., may need manufacturer-specific APIs
        // This is a basic implementation - can be extended
    }
}
