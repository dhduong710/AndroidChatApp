package com.example.hustchat.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.hustchat.MainActivity
import com.example.hustchat.R
import com.example.hustchat.adapter.RequestAdapter
import com.example.hustchat.databinding.FragmentMeBinding
import com.example.hustchat.utils.ImageUtils
import com.example.hustchat.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager


class MeFragment : Fragment() {
    private lateinit var binding: FragmentMeBinding
    private val viewModel: MainViewModel by viewModels() // Use a shared ViewModel
    private lateinit var requestAdapter: RequestAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Adapter
        requestAdapter = RequestAdapter { request ->
            viewModel.acceptRequest(request)
        }
        binding.rvFriendRequests.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = requestAdapter
        }

        // LOAD USER INFORMATION
        loadUserInfo()

        // BUTTON CLICKS
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_meFragment_to_editProfileFragment)
        }

        // OBSERVE DATA
        viewModel.friendRequests.observe(viewLifecycleOwner) { requests ->
            requestAdapter.submitList(requests)
            val visibility = View.VISIBLE
            binding.tvReqHeader.visibility = visibility
            binding.rvFriendRequests.visibility = visibility
        }


        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserInfo() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            binding.tvUsername.text = currentUser.email ?: "Loading..."

            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (isAdded && documentSnapshot.exists()) {

                        val username = documentSnapshot.getString("username") ?: "User"
                        val avatarUrl = documentSnapshot.getString("avatarUrl") // Can be null

                        // Set username
                        binding.tvUsername.text = username

                        // Generate final URL for Glide
                        val finalUrl = ImageUtils.getAvatarUrl(username, avatarUrl)

                        // Load image with Glide
                        Glide.with(this)
                            .load(finalUrl)
                            .placeholder(R.drawable.ic_person) // Set a placeholder
                            .circleCrop()
                            .into(binding.ivAvatar)
                    }
                }
                .addOnFailureListener {
                    if (isAdded) {
                        binding.tvUsername.text = currentUser.email ?: "User"
                        // Set a default avatar on failure
                        binding.ivAvatar.setImageResource(R.drawable.ic_person)
                    }
                }
        } else {
            binding.tvUsername.text = "User"
            binding.ivAvatar.setImageResource(R.drawable.ic_person)
        }
    }


    override fun onResume() {
        super.onResume()
        // Reload data every time this screen is entered
        viewModel.loadFriendRequests()
        loadUserInfo() // Also reload user info in case it was just edited
    }
}
