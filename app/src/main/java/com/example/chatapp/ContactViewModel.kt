package com.example.chatapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContactViewModel : ViewModel() {

    // Get instances of Firebase services.
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // Get Auth to identify the current user.

    // --- LiveData for UI State ---

    // Private MutableLiveData to hold the list of users found in a search.
    private val _users = MutableLiveData<List<User>>()
    // Public, read-only LiveData that the UI (Fragment) can observe for changes.
    val users: LiveData<List<User>> get() = _users

    // Private MutableLiveData to hold any error messages.
    private val _errorMessage = MutableLiveData<String>()
    // Public, read-only LiveData for the UI to observe error messages.
    val errorMessage: LiveData<String> get() = _errorMessage

    // LiveData to report the status of a sent friend request (True = success).
    private val _sendRequestStatus = MutableLiveData<Boolean>()
    val sendRequestStatus: LiveData<Boolean> get() = _sendRequestStatus

    /**
     * Searches for a user in the Firestore database by their email address.
     * @param email The email address to search for.
     */
    fun searchUser(email: String) {
        // Do not proceed if the search query is blank.
        if (email.isBlank()) return

        // Get the current user's ID to exclude them from search results.
        val currentUserId = auth.currentUser?.uid

        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                val userList = ArrayList<User>()
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    // Exclude the current user from the search results.
                    if (user.uid != currentUserId) {
                        userList.add(user)
                    }
                }
                if (userList.isEmpty()) {
                    _errorMessage.value = "No user found!"
                }
                _users.value = userList
            }
            .addOnFailureListener {
                _errorMessage.value = "Search failed: ${it.message}"
            }
    }

    // --- Send a Friend Request ---
    /**
     * Creates and sends a friend request to another user.
     * @param receiver The User object of the person receiving the request.
     */
    fun sendFriendRequest(receiver: User) {
        // Ensure the current user is logged in.
        val currentUserId = auth.currentUser?.uid ?: return
        val currentUser = auth.currentUser

        // Create a map to hold the friend request data.
        val requestMap = hashMapOf(
            "senderId" to currentUserId,
            "senderName" to (currentUser?.displayName ?: "Unknown"),
            "senderEmail" to (currentUser?.email ?: ""),
            "receiverId" to receiver.uid,
            "status" to "pending", // The initial status of the request.
            "timestamp" to System.currentTimeMillis()
        )

        // Save the request in a sub-collection named "friendRequests" under the RECEIVER's document.
        // The path will be: users/{receiverId}/friendRequests/{senderId}
        // Using the sender's UID as the document ID prevents duplicate requests from the same user.
        firestore.collection("users").document(receiver.uid)
            .collection("friendRequests").document(currentUserId)
            .set(requestMap)
            .addOnSuccessListener {
                // If successful, update the status to notify the UI.
                _sendRequestStatus.value = true
            }
            .addOnFailureListener { e ->
                // If it fails, post an error message.
                _errorMessage.value = "Failed to send request: ${e.message}"
            }
    }
}
