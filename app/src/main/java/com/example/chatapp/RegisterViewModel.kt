package com.example.chatapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterViewModel : ViewModel() {

    // Get instances of Firebase services.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // --- LiveData for UI State ---

    // Private MutableLiveData that holds the result of the registration operation.
    // This will be observed by the Fragment to know when to navigate away.
    private val _registerResult = MutableLiveData<Boolean>()
    // Public, read-only LiveData exposed to the Fragment.
    val registerResult: LiveData<Boolean> get() = _registerResult

    // Private MutableLiveData to hold any error message to be displayed.
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Private MutableLiveData to control the visibility of a loading indicator (e.g., ProgressBar).
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

        // 1. Create the user account with Firebase Authentication using email and password.
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // If authentication is successful, get the newly created user and their UID.
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid ?: ""

                    // 2. Update the user's profile in Firebase Authentication to set their display name.
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                    firebaseUser?.updateProfile(profileUpdates)

                    // 3. SAVE THE USER'S INFORMATION TO THE FIRESTORE DATABASE.
                    // This is crucial for storing additional user data (like email) and for other users to find them.
                    val user = User(uid, username, email)

                    // Save the user object to the "users" collection.
                    // The document ID is set to the user's UID for easy and direct lookup later.
                    firestore.collection("users").document(uid).set(user)
                        .addOnSuccessListener {
                            // If saving to Firestore is successful, the entire process is complete.
                            _isLoading.value = false // Hide the loading indicator on success.
                            _registerResult.value = true // Signal to the UI that registration succeeded.
                        }
                        .addOnFailureListener { e ->
                            // IMPORTANT: Handle the case where saving to Firestore fails.
                            _isLoading.value = false // Hide the loading indicator on failure.
                            Log.e("RegisterVM", "Firestore Error: ${e.message}")
                            _errorMessage.value = "Failed to save user data: ${e.message}"

                            // OPTIONAL: If saving to the database fails but authentication succeeded,
                            // you might want to either let the user into the app anyway or delete the
                            // authenticated user to allow them to try registering again.
                            // For now, we show an error to help debug the API issue.
                        }

                } else {
                    // If Firebase Authentication fails (e.g., duplicate email, weak password).
                    _isLoading.value = false // Hide the loading indicator on auth failure.
                    // Post the error message from the exception to be shown in the UI.
                    _errorMessage.value = task.exception?.message ?: "Registration failed"
                }
            }
    }
}
