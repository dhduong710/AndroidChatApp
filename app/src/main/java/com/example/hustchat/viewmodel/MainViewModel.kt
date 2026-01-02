package com.example.hustchat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustchat.model.User
import com.example.hustchat.repository.UserRepository
import kotlinx.coroutines.launch

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
                _toastMessage.value = "A request has been sent to ${user.username}"
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            }
        }
    }
}