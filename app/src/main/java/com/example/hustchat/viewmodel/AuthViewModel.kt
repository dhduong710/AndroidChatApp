package com.example.hustchat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hustchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // LiveData for the UI to observe the state (Lesson 8)
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> get() = _authState

    // Login Function
    fun login(emailOrPhone: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // If the user enters a phone number, must find the corresponding email in Firestore first
                // because Firebase Auth Email/Pass requires an email to log in
                var emailToLogin = emailOrPhone
                if (!emailOrPhone.contains("@")) {
                    val querySnapshot = db.collection("users")
                        .whereEqualTo("phoneNumber", emailOrPhone)
                        .get().await()

                    if (querySnapshot.isEmpty) {
                        withContext(Dispatchers.Main) { _authState.value = AuthState.Error("Phone number is not registered!") }
                        return@launch
                    }
                    emailToLogin = querySnapshot.documents[0].getString("email") ?: ""
                }

                auth.signInWithEmailAndPassword(emailToLogin, pass).await()
                withContext(Dispatchers.Main) { _authState.value = AuthState.Success }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) { _authState.value = AuthState.Error("Login failed: ${e.message}") }
            }
        }
    }

    // Register Function
    fun register(username: String, email: String, phone: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Manually check for duplicate strings (Email & Phone) on Firestore (Lesson 9 logic)
                val checkEmail = db.collection("users").whereEqualTo("email", email).get().await()
                if (!checkEmail.isEmpty) {
                    withContext(Dispatchers.Main) { _authState.value = AuthState.Error("This email is already in use!") }
                    return@launch
                }

                val checkPhone = db.collection("users").whereEqualTo("phoneNumber", phone).get().await()
                if (!checkPhone.isEmpty) {
                    withContext(Dispatchers.Main) { _authState.value = AuthState.Error("This phone number is already in use!") }
                    return@launch
                }

                // 2. If no duplicates, create Auth user
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = authResult.user?.uid ?: ""

                // 3. Save User information to Firestore
                val newUser = User(uid, username, email, phone)
                db.collection("users").document(uid).set(newUser).await()

                withContext(Dispatchers.Main) { _authState.value = AuthState.Success }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) { _authState.value = AuthState.Error("Registration failed: ${e.message}") }
            }
        }
    }
}

// Sealed class to manage UI state
sealed class AuthState {
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
