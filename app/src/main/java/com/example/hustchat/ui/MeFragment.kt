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

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hustchat.adapter.RequestAdapter
import com.example.hustchat.viewmodel.MainViewModel

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

        // Setup User Info (Assuming a simple case of getting from currentUser)
        val currentUser = FirebaseAuth.getInstance().currentUser
        // Can fetch more detailed username information from Firestore if want to display it accurately
        binding.tvUsername.text = currentUser?.email ?: "User"

        // Old Logout logic
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        // Observe data
        viewModel.friendRequests.observe(viewLifecycleOwner) { requests ->
            requestAdapter.submitList(requests)
            // Hide/show the header if the list is empty
            // binding.tvReqHeader.visibility = if (requests.isEmpty()) View.GONE else View.VISIBLE
        }


        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onResume() {
        super.onResume()
        // Reload every time this screen is entered
        viewModel.loadFriendRequests()
    }
}
