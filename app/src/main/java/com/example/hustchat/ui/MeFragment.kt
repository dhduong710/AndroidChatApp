package com.example.hustchat.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hustchat.MainActivity
import com.example.hustchat.databinding.FragmentMeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hustchat.adapter.RequestAdapter
import com.example.hustchat.viewmodel.MainViewModel

import androidx.navigation.fragment.findNavController
import com.example.hustchat.R


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
            binding.tvReqHeader.visibility = View.VISIBLE
        }


        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserInfo() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Set email as a temporary value while loading username
            binding.tvUsername.text = currentUser.email ?: "Loading..."

            // Query Firestore to get the current user's document
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    // Check if the fragment is still added before updating the UI
                    if (isAdded && documentSnapshot.exists()) {
                        // Get the "username" field from the document
                        val username = documentSnapshot.getString("username")
                        // Update the TextView with the fetched username
                        binding.tvUsername.text = username ?: currentUser.email // Fallback to email if username is null
                    }
                }
                .addOnFailureListener {
                    // Handle failure, by still displaying the email
                    if (isAdded) {
                        binding.tvUsername.text = currentUser.email ?: "User"
                    }
                }
        } else {
            binding.tvUsername.text = "User"
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data every time this screen is entered
        viewModel.loadFriendRequests()
        loadUserInfo() // Also reload user info in case it was just edited
    }
}
