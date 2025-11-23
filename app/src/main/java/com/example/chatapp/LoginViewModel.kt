package com.example.chatapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    // Get an instance of FirebaseAuth to interact with Firebase Authentication services.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // LiveData for UI State

    // Private MutableLiveData that holds the result of the login/registration operation.
    // True for success, false for failure.
    private val _loginResult = MutableLiveData<Boolean>()
    // Public LiveData that the Fragment can observe. This is read-only to prevent external modification.
    val loginResult: LiveData<Boolean> get() = _loginResult

    // Holds any error message to be displayed to the user. Nullable to clear the error.
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Indicates whether a long-running operation (like login or registration) is in progress.
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    /**
     * Handles the user login process.
     * @param email The user's email.
     * @param pass The user's password.
     */
    fun login(email: String, pass: String) {
        // Validate input fields to ensure they are not empty.
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true // Set loading state to true.

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                _isLoading.value = false // Set loading state to false once the task is complete.
                if (task.isSuccessful) {
                    _loginResult.value = true // Notify the UI that login was successful.
                } else {
                    _loginResult.value = false // Notify the UI that login failed.
                    // Provide a descriptive error message from the Firebase exception.
                    _errorMessage.value = task.exception?.message ?: "Login failed"
                }
            }
    }

    /**
     * Handles the user registration process.
     * @param email The new user's email.
     * @param pass The new user's password.
     */
    fun register(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }
        _isLoading.value = true

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    // After successful registration, automatically log the user in.
                    _loginResult.value = true
                } else {
                    _errorMessage.value = task.exception?.message ?: "Registration failed"
                }
            }
    }

    /**
     * Checks if a user is already authenticated when the app starts.
     * This is used to bypass the login screen if the user is already signed in.
     */
    fun checkCurrentUser() {
        if (auth.currentUser != null) {
            // If a user is found, post a success result to navigate to the home screen.
            _loginResult.value = true
        }
    }
}
