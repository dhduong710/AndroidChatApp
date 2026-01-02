package com.example.hustchat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hustchat.R
import com.example.hustchat.adapter.ConversationAdapter
import com.example.hustchat.databinding.FragmentMessagesBinding
import com.example.hustchat.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

class MessagesFragment : Fragment() {
    private lateinit var binding: FragmentMessagesBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: ConversationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup Adapter
        adapter = ConversationAdapter { conversation ->
            // Package the other person's ID and name to send to the chat screen
            val bundle = Bundle().apply {
                putString("conversationId", conversation.id)
                putString("otherName", conversation.otherUser?.username ?: "User")
            }

            // Screen switching command
            findNavController().navigate(R.id.action_messagesFragment_to_chatFragment, bundle)
        }

        binding.rvConversations.layoutManager = LinearLayoutManager(context)
        binding.rvConversations.adapter = adapter

        // 2. Plus Menu Logic
        binding.btnPlus.setOnClickListener { showPlusMenu(it) }

        // 3. Observe Conversations
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModel.conversations.observe(viewLifecycleOwner) { conversations ->
            if (conversations.isEmpty()) {
                // Show an empty state message
                adapter.submitList(emptyList()) // Clear the list if it's empty
                return@observe
            }

            // Logic: The Conversation object only has IDs, need to find the other user's info to display.
            // Iterate through each conversation
            conversations.forEach { chat ->
                // Find the other person's ID (the ID that is not the current user's)
                val otherId = chat.participantIds.find { it != currentUid }

                if (otherId != null) {
                    // Call the function in the ViewModel to get user info from Firestore
                    viewModel.getUserInfo(otherId) { user ->
                        chat.otherUser = user // Assign to the transient variable

                        // Update the adapter's list after loading the info
                        // Create a new list to ensure DiffUtil detects the change
                        adapter.submitList(conversations.toList())
                    }
                }
            }
            // Submit the initial list (may not have names yet, they will update when the callback runs)
            adapter.submitList(conversations)
        }
    }

    private fun showPlusMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add(0, 1, 0, "Add Friend")
        popup.menu.add(0, 2, 1, "Create Group")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    findNavController().navigate(R.id.action_messages_to_addFriend)
                    true
                }
                2 -> {
                    findNavController().navigate(R.id.action_messages_to_createGroup)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
