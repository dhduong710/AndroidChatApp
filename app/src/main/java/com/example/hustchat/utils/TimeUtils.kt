package com.example.hustchat.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {

    // Time format: 14:30
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Full date-time format: 02/01 14:30
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Display logic for the Chat list
    fun getTimeDisplay(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val oneDayInMillis = 24 * 60 * 60 * 1000

        // If within the last 24 hours -> Show time (14:30)
        // If older -> Show Date + Time (02/01 14:30)
        return if (diff < oneDayInMillis) {
            formatTime(timestamp)
        } else {
            formatDateTime(timestamp)
        }
    }

    // Display logic for "Online ... ago"
    fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)

        return when {
            diff < 60 * 1000 -> "Active now" // < 1 minute
            diff < 60 * 60 * 1000 -> "Active ${minutes}m ago"
            diff < 24 * 60 * 60 * 1000 -> "Active ${hours}h ago"
            else -> "Active on ${formatDateTime(timestamp)}"
        }
    }
}
