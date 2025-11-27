package com.example.chatapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterViewModel : ViewModel() {

    // Get an instance of FirebaseAuth to interact with Firebase Authentication services.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    // Initialize an instance of Cloud Firestore to interact with the database.
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // --- LiveData for UI State ---

    // Private MutableLiveData that holds the result of the registration operation.
    // True for success.
    private val _registerResult = MutableLiveData<Boolean>()
    // Public, read-only LiveData that the Fragment can observe.
    val registerResult: LiveData<Boolean> get() = _registerResult

    // Holds any error message to be displayed to the user.
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Indicates whether a long-running operation is in progress.
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

        // 1. Create the user account with Firebase Authentication.
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the newly created user and their unique ID (UID).
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid ?: ""

                    // 2. Update the user's profile display name in Firebase Auth.
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    firebaseUser?.updateProfile(profileUpdates)

                    // 3. SAVE THE USER'S INFORMATION TO THE FIRESTORE DATABASE.
                    // This is crucial for storing additional user data and for other users to find them.
                    val user = User(uid, username, email)

                    // Save the user object to the "users" collection.
                    // The document ID is set to the user's UID for easy lookup.
                    firestore.collection("users").document(uid).set(user)
                        .addOnSuccessListener {
                            // If saving to Firestore is successful, the process is complete.
                            _isLoading.value = false
                            _registerResult.value = true
                        }
                        .addOnFailureListener { e ->
                            // Handle the rare case where saving to Firestore fails.
                            _isLoading.value = false
                            _errorMessage.value = "Save to DB failed: ${e.message}"
                            // Even though saving to the database failed, authentication was successful.
                            // In a real-world scenario, you might need rollback logic, but for now,
                            // we'll consider the registration successful to allow the user to log in.
                            _registerResult.value = true
                        }

                } else {
                    // If account creation fails, set loading to false and post the error message.
                    _isLoading.value = false
                    _errorMessage.value = task.exception?.message ?: "Registration failed"
                }
            }
    }
}
