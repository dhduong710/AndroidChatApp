package com.example.hustchat.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val avatarUrl: String = "", // Default is empty, the UI will automatically handle the default image

    val status: String = "offline", // "online" or "offline"
    val lastSeen: Long = 0
)