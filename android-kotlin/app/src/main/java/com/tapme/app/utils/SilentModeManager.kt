package com.tapme.app.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

/**
 * Manages Silent Mode / Do Not Disturb settings
 * Allows users to set quiet hours and receive morning summaries
 */
class SilentModeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "silent_mode_prefs"
        private const val KEY_ENABLED = "silent_mode_enabled"
        private const val KEY_START_HOUR = "start_hour"
        private const val KEY_START_MINUTE = "start_minute"
        private const val KEY_END_HOUR = "end_hour"
        private const val KEY_END_MINUTE = "end_minute"
        private const val KEY_MORNING_SUMMARY_ENABLED = "morning_summary_enabled"
        
        // Default: 10 PM to 8 AM
        private const val DEFAULT_START_HOUR = 22
        private const val DEFAULT_START_MINUTE = 0
        private const val DEFAULT_END_HOUR = 8
        private const val DEFAULT_END_MINUTE = 0
    }
    
    /**
     * Check if silent mode is currently active
     */
    fun isSilentModeActive(): Boolean {
        if (!isEnabled()) return false
        
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        
        val startHour = getStartHour()
        val startMinute = getStartMinute()
        val endHour = getEndHour()
        val endMinute = getEndMinute()
        
        val currentTime = currentHour * 60 + currentMinute
        val startTime = startHour * 60 + startMinute
        val endTime = endHour * 60 + endMinute
        
        // Handle overnight silent mode (e.g., 10 PM to 8 AM)
        return if (startTime > endTime) {
            // Overnight: current time is after start OR before end
            currentTime >= startTime || currentTime < endTime
        } else {
            // Same day: current time is between start and end
            currentTime >= startTime && currentTime < endTime
        }
    }
    
    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)
    
    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
    
    fun getStartHour(): Int = prefs.getInt(KEY_START_HOUR, DEFAULT_START_HOUR)
    fun getStartMinute(): Int = prefs.getInt(KEY_START_MINUTE, DEFAULT_START_MINUTE)
    fun getEndHour(): Int = prefs.getInt(KEY_END_HOUR, DEFAULT_END_HOUR)
    fun getEndMinute(): Int = prefs.getInt(KEY_END_MINUTE, DEFAULT_END_MINUTE)
    
    fun setQuietHours(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        prefs.edit()
            .putInt(KEY_START_HOUR, startHour)
            .putInt(KEY_START_MINUTE, startMinute)
            .putInt(KEY_END_HOUR, endHour)
            .putInt(KEY_END_MINUTE, endMinute)
            .apply()
    }
    
    fun isMorningSummaryEnabled(): Boolean = prefs.getBoolean(KEY_MORNING_SUMMARY_ENABLED, true)
    
    fun setMorningSummaryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MORNING_SUMMARY_ENABLED, enabled).apply()
    }
    
    /**
     * Get formatted quiet hours string (e.g., "10:00 PM - 8:00 AM")
     */
    fun getQuietHoursString(): String {
        val startHour = getStartHour()
        val startMinute = getStartMinute()
        val endHour = getEndHour()
        val endMinute = getEndMinute()
        
        val startPeriod = if (startHour >= 12) "PM" else "AM"
        val endPeriod = if (endHour >= 12) "PM" else "AM"
        
        val displayStartHour = if (startHour > 12) startHour - 12 else if (startHour == 0) 12 else startHour
        val displayEndHour = if (endHour > 12) endHour - 12 else if (endHour == 0) 12 else endHour
        
        val startMinuteStr = String.format("%02d", startMinute)
        val endMinuteStr = String.format("%02d", endMinute)
        
        return "$displayStartHour:$startMinuteStr $startPeriod - $displayEndHour:$endMinuteStr $endPeriod"
    }
}
