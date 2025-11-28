package com.example.chatapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// It looks like FriendRequestAdapter is needed but not imported.
// Make sure you have created and imported this adapter.
import com.example.chatapp.adapter.FriendRequestAdapter
import com.example.chatapp.UserAdapter
import com.example.chatapp.databinding.FragmentContactBinding
import com.example.chatapp.ContactViewModel

class ContactFragment : Fragment() {

    // Use the 'by viewModels()' Kotlin property delegate to get a reference to the ViewModel.
    // This automatically handles the lifecycle of the ViewModel.
    private val viewModel: ContactViewModel by viewModels()

    // Declare the adapter for the user search results RecyclerView.
    private lateinit var userAdapter: UserAdapter

    // Declare the adapter for the friend requests RecyclerView.
    private lateinit var requestAdapter: FriendRequestAdapter

    // Using view binding to safely access views.
    private var _binding: FragmentContactBinding? = null

    // This non-nullable property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using view binding.
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Setup for User Search ---
        setupUserSearch()

        // --- Setup for Friend Requests ---
        setupFriendRequests()

        // --- Observe LiveData from ViewModel ---
        observeViewModel()
    }

    /**
     * Sets up the RecyclerView and listeners for the user search functionality.
     */
    private fun setupUserSearch() {
        // Initialize the adapter for search results.
        userAdapter = UserAdapter()
        // Set the LayoutManager for the RecyclerView.
        binding.rvUserList.layoutManager = LinearLayoutManager(context)
        // Attach the adapter to the RecyclerView.
        binding.rvUserList.adapter = userAdapter

        // Set a click listener for the search button.
        binding.btnSearch.setOnClickListener {
            val email = binding.etSearchEmail.text.toString().trim()
            viewModel.searchUser(email)
        }

        // Handle the click event for the "Add Friend" button from the adapter.
        userAdapter.onAddFriendClick = { targetUser ->
            // Call the ViewModel to send the friend request.
            viewModel.sendFriendRequest(targetUser)
        }
    }

    /**
     * Sets up the RecyclerView and listeners for displaying and handling incoming friend requests.
     */
    private fun setupFriendRequests() {
        // 1. Setup RecyclerView for Friend Requests.
        requestAdapter = FriendRequestAdapter()
        binding.rvFriendRequests.layoutManager = LinearLayoutManager(context)
        binding.rvFriendRequests.adapter = requestAdapter

        // 2. Fetch the friend requests as soon as the screen is visible.
        viewModel.getFriendRequests()

        // 3. Handle the "Accept" button click from the adapter.
        requestAdapter.onAcceptClick = { request ->
            viewModel.acceptFriendRequest(request)
        }
    }

    /**
     * Sets up observers for all LiveData objects from the ViewModel.
     */
    private fun observeViewModel() {
        // Observe the list of found users from the search.
        viewModel.users.observe(viewLifecycleOwner) { users ->
            userAdapter.setUsers(users)
        }

        // Observe any error messages.
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe the status of a sent friend request.
        viewModel.sendRequestStatus.observe(viewLifecycleOwner) { isSent ->
            if (isSent) {
                Toast.makeText(context, "Friend request sent!", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe the list of incoming friend requests.
        viewModel.friendRequests.observe(viewLifecycleOwner) { requests ->
            requestAdapter.setRequests(requests)
        }

        // Observe when a new chat is successfully created after accepting a friend request.
        viewModel.chatCreatedId.observe(viewLifecycleOwner) { chatId ->
            if (chatId != null) {
                Toast.makeText(context, "Friend added! Starting chat.", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to the ChatFragment.

            }
        }
    }

    /**
     * Clean up the binding instance when the view is destroyed to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
