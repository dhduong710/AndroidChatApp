package com.example.chatapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.model.User
import com.google.firebase.firestore.FirebaseFirestore

class ContactViewModel : ViewModel() {

    // Get an instance of the Cloud Firestore database.
    private val firestore = FirebaseFirestore.getInstance()

    // --- LiveData for UI State ---

    // Private MutableLiveData to hold the list of users found in a search.
    private val _users = MutableLiveData<List<User>>()
    // Public, immutable LiveData that the UI (Fragment) can observe for changes.
    val users: LiveData<List<User>> get() = _users

    // Private MutableLiveData to hold any error messages.
    private val _errorMessage = MutableLiveData<String>()
    // Public, immutable LiveData for the UI to observe error messages.
    val errorMessage: LiveData<String> get() = _errorMessage

    /**
     * Searches for a user in the Firestore database by their email address.
     * @param email The email address to search for.
     */
    fun searchUser(email: String) {
        // Do not proceed if the search query is blank.
        if (email.isBlank()) {
            _errorMessage.value = "Please enter an email to search."
            return
        }

        // Query the "users" collection in Firestore.
        firestore.collection("users")
            // Find documents where the "email" field exactly matches the search query.
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                // This block is executed if the query is successful.
                val userList = ArrayList<User>()
                for (document in documents) {
                    // Convert each Firestore document into a User object.
                    val user = document.toObject(User::class.java)
                    // (Optional) You might want to add logic here to filter out the current user
                    // from the search results, e.g., if (user.uid != currentUser.uid)
                    userList.add(user)
                }

                if (userList.isEmpty()) {
                    // If no users were found, post an error message.
                    _errorMessage.value = "No user found with that email."
                }
                // Update the LiveData with the list of found users.
                // The UI will be automatically updated.
                _users.value = userList
            }
            .addOnFailureListener { e ->
                // This block is executed if the query fails.
                _errorMessage.value = "Search failed: ${e.message}"
            }
    }
}
