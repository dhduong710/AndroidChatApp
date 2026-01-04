package com.example.hustchat.repository

import com.example.hustchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

import com.example.hustchat.model.FriendRequest
import com.example.hustchat.model.Conversation

import kotlinx.coroutines.flow.callbackFlow

import kotlinx.coroutines.channels.awaitClose

import com.example.hustchat.model.Message


class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Search User (Excluding users who are already friends)
    suspend fun searchUsers(query: String): List<User> {
        val currentUid = auth.currentUser?.uid ?: return emptyList()
        val users = mutableListOf<User>()

        try {
            // Get the current friends list first to filter them out
            val friendsSnapshot = db.collection("users").document(currentUid)
                .collection("friends").get().await()
            val friendIds = friendsSnapshot.documents.map { it.id }.toSet()

            // Search logic
            val emailQuery = db.collection("users").whereEqualTo("email", query).get().await()
            val phoneQuery = db.collection("users").whereEqualTo("phoneNumber", query).get().await()
            val allDocs = emailQuery.documents + phoneQuery.documents

            for (doc in allDocs) {
                val user = doc.toObject(User::class.java)
                // Condition: Not me AND not already a friend
                if (user != null && user.uid != currentUid && !friendIds.contains(user.uid)) {
                    users.add(user)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return users.distinctBy { it.uid }
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

    // Creates a unified chat room ID from 2 UIDs (so that when A chats with B or B chats with A, the same ID is generated)
    private fun getConversationId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    // Accept friend request -> Create a conversation immediately
    suspend fun acceptFriendRequest(request: FriendRequest) {
        val currentUid = auth.currentUser?.uid ?: return
        val partnerId = request.senderId

        val batch = db.batch()
        val timestamp = System.currentTimeMillis()

        // 1. Delete the friend request
        val requestRef = db.collection("friend_requests").document(request.id)
        batch.delete(requestRef)

        // 2. Add to Friends (both ways)
        val myFriendRef = db.collection("users").document(currentUid)
            .collection("friends").document(partnerId)
        batch.set(myFriendRef, request.senderUser!!) // Save basic info

        // Need to fetch my own info to save on the other side
        val meDoc = db.collection("users").document(currentUid).get().await()
        val me = meDoc.toObject(User::class.java)!!

        val otherFriendRef = db.collection("users").document(partnerId)
            .collection("friends").document(currentUid)
        batch.set(otherFriendRef, me)

        // 3. CREATE CONVERSATION
        val conversationId = getConversationId(currentUid, partnerId)
        val conversationRef = db.collection("conversations").document(conversationId)

        val newConversation = hashMapOf(
            "id" to conversationId,
            "participantIds" to listOf(currentUid, partnerId),
            "lastMessage" to "You are now friends. Say hello!",
            "timestamp" to timestamp
        )
        // Use set with merge=true to avoid overwriting if an old chat exists (in case of un-friending and re-friending)
        batch.set(conversationRef, newConversation, SetOptions.merge())

        // 4. Create the first system message in the "messages" sub-collection
        val sysMsgRef = conversationRef.collection("messages").document()
        val sysMsg = hashMapOf(
            "senderId" to "SYSTEM",
            "content" to "You are now friends. Say hello!",
            "timestamp" to timestamp
        )
        batch.set(sysMsgRef, sysMsg)

        batch.commit().await()
    }

    // Get the list of Conversations in Realtime (using Kotlin Flow and Firestore)
    fun getConversationsLive(): kotlinx.coroutines.flow.Flow<List<Conversation>> = kotlinx.coroutines.flow.callbackFlow {
        val currentUid = auth.currentUser?.uid ?: return@callbackFlow

        val listener = db.collection("conversations")
            .whereArrayContains("participantIds", currentUid) // Query for chats where the current user is a participant
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Close the flow with an error
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val conversations = mutableListOf<Conversation>()
                    // Since Firestore doesn't support joins, we have to manually load the other user's info.
                    // However, to be fast, we will return the list of conversations first,
                    // and the ViewModel will be responsible for loading the user info later.
                    for (doc in snapshot.documents) {
                        val chat = doc.toObject(Conversation::class.java)
                        if (chat != null) {
                            conversations.add(chat)
                        }
                    }
                    trySend(conversations) // Send the latest list to the flow
                }
            }
        // When the flow is cancelled, remove the Firestore listener to prevent memory leaks
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(conversationId: String, content: String) {
        val currentUid = auth.currentUser?.uid ?: return
        val timestamp = System.currentTimeMillis()

        val msgRef = db.collection("conversations").document(conversationId)
            .collection("messages").document() // Generate ID automatically

        val message = Message(msgRef.id, currentUid, content, timestamp)

        val batch = db.batch()
        // 1. Save the message to the sub-collection
        batch.set(msgRef, message)

        // 2. Update the last message in Conversation (for display in the conversation list)
        val convRef = db.collection("conversations").document(conversationId)
        batch.update(convRef, mapOf(
            "lastMessage" to content,
            "timestamp" to timestamp
        ))

        batch.commit().await()
    }

    // Listen to messages in real-time
    fun getMessagesLive(conversationId: String): kotlinx.coroutines.flow.Flow<List<Message>> = callbackFlow {
        val listener = db.collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }

                if (snapshot != null) {
                    val msgs = snapshot.toObjects(Message::class.java)
                    trySend(msgs)
                }
            }
        awaitClose { listener.remove() }
    }

    // Get the list of friends (from the friends sub-collection)
    suspend fun getFriends(): List<User> {
        val currentUid = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("users").document(currentUid)
            .collection("friends").get().await()
        return snapshot.toObjects(User::class.java)
    }

    // Create Group
    suspend fun createGroup(groupName: String, memberIds: List<String>) {
        val currentUid = auth.currentUser?.uid ?: return
        val timestamp = System.currentTimeMillis()

        // Generate a random ID for the Group
        val groupRef = db.collection("conversations").document()

        // Participant list includes: Me + the friends I've selected
        val allParticipants = memberIds.toMutableList()
        allParticipants.add(currentUid)

        val groupChat = hashMapOf(
            "id" to groupRef.id,
            "participantIds" to allParticipants,
            "type" to "group", // Mark as Group
            "groupName" to groupName,
            "lastMessage" to "Group \"$groupName\" has been created.",
            "timestamp" to timestamp
        )

        val batch = db.batch()
        batch.set(groupRef, groupChat)

        // Create system message
        val msgRef = groupRef.collection("messages").document()
        val sysMsg = hashMapOf(
            "senderId" to "SYSTEM",
            "content" to "Group \"$groupName\" has been created.",
            "timestamp" to timestamp
        )
        batch.set(msgRef, sysMsg)

        batch.commit().await()
    }

    // Update user profile (Username)
    suspend fun updateUserProfile(newUsername: String) {
        val uid = auth.currentUser?.uid ?: return
        val updates = hashMapOf<String, Any>(
            "username" to newUsername
        )
        // If there's an avatarUrl, add it to the updates map here
        db.collection("users").document(uid).update(updates).await()
    }

    // Update group profile (Group Name)
    suspend fun updateGroupProfile(groupId: String, newName: String) {
        val updates = hashMapOf<String, Any>(
            "groupName" to newName
        )
        // If there's a groupAvatarUrl, add it here
        db.collection("conversations").document(groupId).update(updates).await()
    }
}
