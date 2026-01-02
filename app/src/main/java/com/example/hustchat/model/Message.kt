package com.example.hustchat.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val type: String = "text"
)