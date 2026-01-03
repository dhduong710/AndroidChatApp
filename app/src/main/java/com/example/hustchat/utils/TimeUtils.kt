package com.example.hustchat.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {

    // Formats to: 18:36
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Formats to: 18/09/2036
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Checks if the timestamp is "Today" (since 00:00)
    fun isToday(timestamp: Long): Boolean {
        val now = Calendar.getInstance()
        val timeToCheck = Calendar.getInstance()
        timeToCheck.timeInMillis = timestamp

        return now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR)
    }

    // Checks if two timestamps are on the same day (to decide whether to show a Date Header)
    fun isSameDay(t1: Long, t2: Long): Boolean {
        // A timestamp of 0 means there's no previous message, so they can't be on the same day.
        if (t1 == 0L || t2 == 0L) return false

        val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // Display format for the Date Header in Chat (e.g., "Today" or "18/09/2036")
    fun getDateHeaderDisplay(timestamp: Long): String {
        return if (isToday(timestamp)) "Today" else formatDate(timestamp)
    }

    // Display format for the conversation list in MessagesFragment
    fun getTimeDisplay(timestamp: Long): String {
        return if (isToday(timestamp)) formatTime(timestamp) else formatDate(timestamp)
    }

    // Display logic for "Active ... ago" status
    fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)

        return when {
            diff < 60 * 1000 -> "Active now" // Less than 1 minute
            diff < 60 * 60 * 1000 -> "Active ${minutes}m ago"
            diff < 24 * 60 * 60 * 1000 -> "Active ${hours}h ago"
            else -> "Active on ${formatDate(timestamp)}"
        }
    }
}
