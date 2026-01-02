package com.example.hustchat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hustchat.adapter.UserAdapter
import com.example.hustchat.databinding.FragmentAddFriendBinding
import com.example.hustchat.viewmodel.MainViewModel

class AddFriendFragment : Fragment() {

    private lateinit var binding: FragmentAddFriendBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        adapter = UserAdapter { user ->
            // When click the "Add Friend" button
            viewModel.sendFriendRequest(user)
        }
        binding.rvUserResults.adapter = adapter

        // Handle Search
        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchUser.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchUser(query)
            }
        }

        // Observe Data
        viewModel.searchResults.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}