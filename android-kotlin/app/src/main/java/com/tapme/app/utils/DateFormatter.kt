package com.tapme.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    private val months = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    fun formatTimeAgo(dateTime: Date): String {
        val now = Date()
        val difference = now.time - dateTime.time
        val minutes = difference / (60 * 1000)
        val hours = difference / (60 * 60 * 1000)
        val days = difference / (24 * 60 * 60 * 1000)

        return when {
            minutes < 1 -> "just now"
            hours < 1 -> "${minutes}m ago"
            days < 1 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> formatDate(dateTime)
        }
    }

    fun formatDateTime(dateTime: Date): String {
        val calendar = Calendar.getInstance().apply { time = dateTime }
        val month = months[calendar.get(Calendar.MONTH)]
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        val hour12 = if (hour == 0) 12 else hour

        return "$month $day, $year • $hour12:${String.format("%02d", minute)} $amPm"
    }

    private fun formatDate(dateTime: Date): String {
        val calendar = Calendar.getInstance().apply { time = dateTime }
        val month = months[calendar.get(Calendar.MONTH)]
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$month $day"
    }
}
