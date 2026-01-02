package com.example.hustchat.repository

import com.example.hustchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Search for a user by exact email or phone number
    suspend fun searchUsers(query: String): List<User> {
        val users = mutableListOf<User>()
        val currentUid = auth.currentUser?.uid ?: return emptyList()

        try {
            // Search by Email
            val emailQuery = db.collection("users")
                .whereEqualTo("email", query)
                .get().await()

            // Search by Phone
            val phoneQuery = db.collection("users")
                .whereEqualTo("phoneNumber", query)
                .get().await()

            // Merge results (excluding the current user)
            val allDocs = emailQuery.documents + phoneQuery.documents
            for (doc in allDocs) {
                val user = doc.toObject(User::class.java)
                if (user != null && user.uid != currentUid) {
                    users.add(user)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return users.distinctBy { it.uid } // Remove duplicates if found in both queries
    }

    // Send a friend request
    suspend fun sendFriendRequest(targetUserId: String) {
        val currentUid = auth.currentUser?.uid ?: return

        val request = hashMapOf(
            "senderId" to currentUid,
            "receiverId" to targetUserId,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )

        // Save to the "friend_requests" collection
        db.collection("friend_requests")
            .add(request)
            .await()
    }
}
