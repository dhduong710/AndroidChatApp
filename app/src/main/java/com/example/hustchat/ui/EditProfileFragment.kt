package com.example.hustchat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hustchat.R
import com.example.hustchat.databinding.FragmentEditProfileBinding
import com.example.hustchat.utils.ImageUtils
import com.example.hustchat.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Load current user profile (name and avatar)
        loadUserProfile()

        binding.btnChangeAvatar.setOnClickListener {
            // Since Storage is not used, we just show a message
            Toast.makeText(context, "Feature not available", Toast.LENGTH_SHORT).show()
        }

        binding.btnSave.setOnClickListener {
            val newName = binding.etUsername.text.toString().trim()
            if (newName.isNotEmpty()) {
                viewModel.updateUserProfile(newName)
                // Show a success message and go back
                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * This function loads user information from Firestore, including username and avatarUrl,
     * then uses Glide to display the avatar.
     */
    private fun loadUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                // Check if the fragment is still added before updating the UI
                if (isAdded && document != null) {
                    val username = document.getString("username") ?: "User"
                    val avatarUrl = document.getString("avatarUrl") // Can be null

                    // 1. Fill the username into the EditText
                    binding.etUsername.setText(username)

                    // 2. Create the final avatar URL
                    val finalUrl = ImageUtils.getAvatarUrl(username, avatarUrl)

                    // 3. Use Glide to load and display the profile picture
                    Glide.with(this)
                        .load(finalUrl)
                        .placeholder(R.drawable.ic_person) // Placeholder image while loading
                        .circleCrop() // Crop the image into a circle
                        .into(binding.ivAvatar) // Display in the ImageView
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(context, "Failed to load profile data", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
