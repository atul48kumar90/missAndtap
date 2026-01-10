package com.tapme.app.utils

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Creates anticipation by showing time since last tap
 * "2h 15m since last tap" - creates "when will the next one come?" feeling
 */
class AnticipationTimer {
    
    private var handler: Handler? = null
    private var isRunning = false
    private var lastTapTime: Long? = null
    private var onUpdate: ((String) -> Unit)? = null
    
    fun start(lastTapTimestamp: Long?, updateCallback: (String) -> Unit) {
        lastTapTime = lastTapTimestamp
        onUpdate = updateCallback
        isRunning = true
        handler = Handler(Looper.getMainLooper())
        
        updateTimer()
    }
    
    fun stop() {
        isRunning = false
        handler?.removeCallbacksAndMessages(null)
        handler = null
    }
    
    private fun updateTimer() {
        if (!isRunning) return
        
        val timeText = if (lastTapTime != null) {
            formatTimeSince(lastTapTime!!)
        } else {
            "No taps yet"
        }
        
        onUpdate?.invoke(timeText)
        
        // Update every minute
        handler?.postDelayed({
            updateTimer()
        }, 60000) // 1 minute
    }
    
    private fun formatTimeSince(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        
        return when {
            hours > 24 -> {
                val days = hours / 24
                "$days day${if (days > 1) "s" else ""} ago"
            }
            hours > 0 -> {
                "$hours h ${minutes}m ago"
            }
            minutes > 0 -> {
                "$minutes m ago"
            }
            else -> {
                "Just now"
            }
        }
    }
}
