package com.example.hustchat.model

import com.google.firebase.firestore.Exclude


data class Conversation(
    val id: String = "",           // Conversation ID (e.g., a combination of uid1_uid2)
    val participantIds: List<String> = emptyList(), // List of participant UIDs
    val lastMessage: String = "",  // The last message (for preview)
    val timestamp: Long = 0,       // Timestamp of the last message

    val type: String = "single", // "single" or "group"
    val groupName: String = "",  // Use only if the type is "group"

    val groupAvatarUrl: String = "",

    // This field is populated locally in the ViewModel/Repository and is not stored in Firestore
    @get:Exclude var otherUser: User? = null
)
