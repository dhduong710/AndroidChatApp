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
import com.example.chatapp.UserAdapter
import com.example.chatapp.databinding.FragmentContactBinding
import com.example.chatapp.ContactViewModel

class ContactFragment : Fragment() {

    // Use the 'by viewModels()' Kotlin property delegate to get a reference to the ViewModel.
    // This automatically handles the lifecycle of the ViewModel.
    private val viewModel: ContactViewModel by viewModels()

    // Declare the adapter for the RecyclerView. It will be initialized later.
    private lateinit var userAdapter: UserAdapter

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

        // --- Setup RecyclerView for displaying search results ---
        // Initialize the adapter.
        userAdapter = UserAdapter()
        // Set the LayoutManager that the RecyclerView will use.
        binding.rvUserList.layoutManager = LinearLayoutManager(context)
        // Attach the adapter to the RecyclerView.
        binding.rvUserList.adapter = userAdapter

        // --- Handle UI Interactions ---

        // Set a click listener for the search button.
        binding.btnSearch.setOnClickListener {
            // Get the text from the search input field and remove leading/trailing whitespace.
            val email = binding.etSearchEmail.text.toString().trim()
            // Call the ViewModel function to perform the user search.
            viewModel.searchUser(email)
        }

        // --- Observe LiveData from ViewModel ---

        // Observe the 'users' LiveData.
        // When the list of users changes in the ViewModel, this block will be executed.
        viewModel.users.observe(viewLifecycleOwner) { users ->
            // Update the adapter with the new list of users.
            userAdapter.setUsers(users)
        }

        // Observe the 'errorMessage' LiveData.
        // If an error message is posted, display it in a Toast.
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }

        // --- Handle Adapter Item Clicks ---

        // Set the lambda function to be executed when the "Add Friend" button inside a RecyclerView item is clicked.
        userAdapter.onAddFriendClick = { user ->
            // Show a confirmation Toast message.
            Toast.makeText(context, "Friend request sent to ${user.username}", Toast.LENGTH_SHORT).show()
            // TODO: Implement the logic to send a friend request to Firestore.
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
