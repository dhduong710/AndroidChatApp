package com.example.hustchat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hustchat.adapter.UserSelectAdapter
import com.example.hustchat.databinding.FragmentCreateGroupBinding
import com.example.hustchat.repository.UserRepository
import kotlinx.coroutines.launch

class CreateGroupFragment : Fragment() {
    private lateinit var binding: FragmentCreateGroupBinding
    private val repository = UserRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load friend list
        lifecycleScope.launch {
            val friends = repository.getFriends()
            val adapter = UserSelectAdapter(friends)
            binding.rvFriends.layoutManager = LinearLayoutManager(context)
            binding.rvFriends.adapter = adapter

            // Handle the Create button
            binding.btnCreateGroup.setOnClickListener {
                val groupName = binding.etGroupName.text.toString().trim()
                val selectedUsers = adapter.selectedUsers

                if (groupName.isEmpty()) {
                    Toast.makeText(context, "Please enter the group name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (selectedUsers.size < 2) {
                    Toast.makeText(context, "Choose at least two friends", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Call Repository to create a group
                lifecycleScope.launch {
                    val memberIds = selectedUsers.map { it.uid }
                    repository.createGroup(groupName, memberIds)
                    Toast.makeText(context, "Group created successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack() // Return to Messages
                }
            }
        }
    }
}