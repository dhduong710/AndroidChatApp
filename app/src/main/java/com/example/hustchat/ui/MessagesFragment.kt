package com.example.hustchat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hustchat.R
import com.example.hustchat.adapter.ConversationAdapter
import com.example.hustchat.databinding.FragmentMessagesBinding
import com.example.hustchat.model.User
import com.example.hustchat.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

class MessagesFragment : Fragment() {
    private lateinit var binding: FragmentMessagesBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: ConversationAdapter

    // A cache to store user info to avoid losing names on reload
    private val userCache = HashMap<String, User>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup Adapter
        adapter = ConversationAdapter { conversation ->
            val bundle = Bundle().apply {
                putString("conversationId", conversation.id)
                // Prioritize getting the name from the cache to ensure data is available
                val otherId = conversation.participantIds.find { it != FirebaseAuth.getInstance().currentUser?.uid }
                val cachedName = if (otherId != null) userCache[otherId]?.username else null
                putString("otherName", cachedName ?: conversation.otherUser?.username ?: "User")
            }
            findNavController().navigate(R.id.action_messagesFragment_to_chatFragment, bundle)
        }

        binding.rvConversations.layoutManager = LinearLayoutManager(context)
        binding.rvConversations.adapter = adapter

        // 2. ADD A DIVIDER
        val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        binding.rvConversations.addItemDecoration(dividerItemDecoration)

        binding.btnPlus.setOnClickListener { showPlusMenu(it) }

        // 3. Observer logic
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModel.conversations.observe(viewLifecycleOwner) { conversations ->
            // Update the list immediately (even without names)
            adapter.submitList(conversations)

            conversations.forEachIndexed { index, chat ->
                val otherId = chat.participantIds.find { it != currentUid }

                if (otherId != null) {
                    // Check if the user is already in the cache
                    if (userCache.containsKey(otherId)) {
                        // If yes, assign immediately and update the UI
                        chat.otherUser = userCache[otherId]
                        adapter.notifyItemChanged(index)
                    } else {
                        // If not, fetch from the API
                        viewModel.getUserInfo(otherId) { user ->
                            if (user != null) {
                                // Save to cache
                                userCache[otherId] = user
                                chat.otherUser = user
                                // Only refresh this specific item
                                adapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showPlusMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add(0, 1, 0, "Add Friend")
        popup.menu.add(0, 2, 1, "Create Group")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> { findNavController().navigate(R.id.action_messagesFragment_to_addFriend); true }
                2 -> { findNavController().navigate(R.id.action_messagesFragment_to_createGroup); true }
                else -> false
            }
        }
        popup.show()
    }
}
