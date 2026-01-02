package com.example.hustchat.repository

import com.example.hustchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

import com.example.hustchat.model.FriendRequest


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

    // Get the list of friend requests (with sender's information)
    suspend fun getFriendRequests(): List<FriendRequest> {
        val currentUid = auth.currentUser?.uid ?: return emptyList()
        val requests = mutableListOf<FriendRequest>()

        try {
            // 1. Get requests from Firestore
            val snapshot = db.collection("friend_requests")
                .whereEqualTo("receiverId", currentUid)
                .whereEqualTo("status", "pending")
                .get().await()

            for (doc in snapshot.documents) {
                val req = doc.toObject(FriendRequest::class.java)?.copy(id = doc.id)

                // 2. Get the sender's User info to display their name
                if (req != null) {
                    val senderDoc = db.collection("users").document(req.senderId).get().await()
                    req.senderUser = senderDoc.toObject(User::class.java)
                    requests.add(req)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return requests
    }

    // Accept a friend request
    suspend fun acceptFriendRequest(request: FriendRequest) {
        val currentUid = auth.currentUser?.uid ?: return

        // Batch write: Perform multiple operations at once (like a transaction)
        val batch = db.batch()

        // 1. Delete the friend request
        val requestRef = db.collection("friend_requests").document(request.id)
        batch.delete(requestRef)

        // 2. Add the new friend to MY "friends" sub-collection
        val myFriendRef = db.collection("users").document(currentUid)
            .collection("friends").document(request.senderId)
        batch.set(myFriendRef, request.senderUser!!)

        // 3. Add myself to the OTHER PERSON's "friends" sub-collection
        // Need to get my own info first
        val meDoc = db.collection("users").document(currentUid).get().await()
        val me = meDoc.toObject(User::class.java)

        if (me != null) {
            val otherFriendRef = db.collection("users").document(request.senderId)
                .collection("friends").document(currentUid)
            batch.set(otherFriendRef, me)
        }

        batch.commit().await()
    }
}
