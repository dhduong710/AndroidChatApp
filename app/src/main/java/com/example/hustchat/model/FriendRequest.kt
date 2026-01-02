package com.example.hustchat.model

data class FriendRequest(
    val id: String = "",         // Invitation document ID
    val senderId: String = "",
    val receiverId: String = "",
    val status: String = "",     // "pending"
    val timestamp: Long = 0,
    var senderUser: User? = null // Temporary variables to store sender information (Name, Avatar)
)