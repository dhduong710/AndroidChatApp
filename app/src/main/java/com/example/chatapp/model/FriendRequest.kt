package com.example.chatapp.model

/**
 * A data class representing a single friend request.
 * This object holds all the necessary information about a request sent from one user to another.
 */
data class FriendRequest(
    val senderId: String = "",
    val senderName: String = "",
    val senderEmail: String = "",
    val receiverId: String = "",
    val status: String = "",      // The current status of the request (e.g., "pending", "accepted", "declined").

    /**
     * The time when the friend request was sent, typically stored as a Unix timestamp
     * (milliseconds since the epoch). This is very important for several reasons:
     *
     * 1.  **Sorting:** It allows you to sort friend requests chronologically, so the user can
     *     see the newest or oldest requests first.
     * 2.  **Displaying Time:** The timestamp can be converted into a human-readable format
     *     like "5 minutes ago", "yesterday", or "2/26/2024".
     * 3.  **Expiration Logic:** You could implement a feature to automatically expire or
     *     delete requests that are too old (e.g., older than 30 days).
     * 4.  **Debugging:** It provides a precise record of when the event occurred, which is
     *     invaluable for troubleshooting.
     */
    val timestamp: Long = 0
)
