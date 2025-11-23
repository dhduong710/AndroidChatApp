package com.example.chatapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterViewModel : ViewModel() {

    // Get an instance of FirebaseAuth to interact with Firebase Authentication services.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // LiveData for UI State

    // Private MutableLiveData that holds the result of the registration operation.
    // True for success.
    private val _registerResult = MutableLiveData<Boolean>()
    // Public, read-only LiveData that the Fragment can observe.
    val registerResult: LiveData<Boolean> get() = _registerResult

    // Holds any error message to be displayed to the user. Nullable to allow clearing the error.
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Indicates whether a long-running operation (like registration) is in progress.
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    /**
     * Handles the user registration process.
     * @param username The desired display name for the user.
     * @param email The user's email address for authentication.
     * @param pass The user's chosen password.
     */
    fun register(username: String, email: String, pass: String) {
        // First, validate that none of the input fields are blank.
        if (username.isBlank() || email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        // Set the loading state to true to show a progress indicator in the UI.
        _isLoading.value = true

        // Create the user account with Firebase Authentication.
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // If account creation is successful, update the user's profile with the provided username.
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            // The entire process is finished, so set loading state to false.
                            _isLoading.value = false
                            if (updateTask.isSuccessful) {
                                // If the profile update is also successful, notify the UI of success.
                                _registerResult.value = true
                            } else {
                                // This is a rare case where the account is created, but the profile name update fails.
                                // We still consider it a successful registration but log an error.
                                _errorMessage.value = "Account created but failed to set username"
                                _registerResult.value = true
                            }
                        }
                } else {
                    // If account creation fails, set loading to false and post the error message.
                    _isLoading.value = false
                    _errorMessage.value = task.exception?.message ?: "Registration failed"
                }
            }
    }
}
