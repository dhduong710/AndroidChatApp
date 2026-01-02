package com.example.hustchat.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Display logic for the chat list screen
    fun getTimeDisplay(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        // If within 24 hours (simple simulation) -> show time, else show date
        return if (diff < 24 * 60 * 60 * 1000) {
            formatTime(timestamp)
        } else {
            formatDate(timestamp)
        }
    }
}