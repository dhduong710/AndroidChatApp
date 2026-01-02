package com.example.hustchat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustchat.model.User
import com.example.hustchat.repository.UserRepository
import kotlinx.coroutines.launch
import com.example.hustchat.model.FriendRequest

import androidx.lifecycle.asLiveData
import com.example.hustchat.model.Conversation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class MainViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _searchResults = MutableLiveData<List<User>>()
    val searchResults: LiveData<List<User>> = _searchResults

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    fun searchUser(query: String) {
        if (query.isEmpty()) return

        viewModelScope.launch {
            val results = repository.searchUsers(query)
            if (results.isEmpty()) {
                _toastMessage.value = "No users found."
            }
            _searchResults.value = results
        }
    }

    fun sendFriendRequest(user: User) {
        viewModelScope.launch {
            try {
                repository.sendFriendRequest(user.uid)
                // TRANSLATED
                _toastMessage.value = "Friend request sent to ${user.username}"
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            }
        }
    }

    private val _friendRequests = MutableLiveData<List<FriendRequest>>()
    val friendRequests: LiveData<List<FriendRequest>> = _friendRequests

    fun loadFriendRequests() {
        viewModelScope.launch {
            val requests = repository.getFriendRequests()
            _friendRequests.value = requests
        }
    }

    fun acceptRequest(request: FriendRequest) {
        viewModelScope.launch {
            try {
                repository.acceptFriendRequest(request)
                // Reload the list after accepting
                loadFriendRequests()
                _toastMessage.value = "You and ${request.senderUser?.username} are now friends"
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            }
        }
    }

    // Convert the Flow to LiveData for the UI to observe
    val conversations = repository.getConversationsLive().asLiveData()

    // Helper function to load the other user's info for a conversation
    fun getUserInfo(uid: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users").document(uid).get().await()
                onResult(doc.toObject(User::class.java))
            } catch (e: Exception) { onResult(null) }
        }
    }

    // Function to retrieve Realtime messages
    fun getMessages(conversationId: String) = repository.getMessagesLive(conversationId).asLiveData()

    // Message sending function
    fun sendMessage(conversationId: String, content: String) {
        viewModelScope.launch {
            repository.sendMessage(conversationId, content)
        }
    }
}
