package com.example.hustchat.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.hustchat.model.Conversation
import com.example.hustchat.model.User
import com.example.hustchat.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

class MessagesFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: ConversationAdapter

    // A cache to store User info (avoids flickering and repeated loads)
    private val userCache = HashMap<String, User>()

    // Store the original list to support the search feature
    private var fullConversationList: List<Conversation> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupPlusButton()
        observeConversations()
    }

    // Configure RecyclerView & Adapter
    private fun setupRecyclerView() {
        adapter = ConversationAdapter { conversation ->
            // When a conversation is clicked
            val bundle = Bundle().apply {
                putString("conversationId", conversation.id)

                // Logic to get display title: Use group name for groups, friend's name for single chats
                val displayTitle = if (conversation.type == "group") {
                    conversation.groupName
                } else {
                    conversation.otherUser?.username ?: "User"
                }
                putString("otherName", displayTitle)
            }
            findNavController().navigate(R.id.action_messagesFragment_to_chatFragment, bundle)
        }

        binding.rvConversations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MessagesFragment.adapter
            // Add a divider line between items for better visuals
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    // Configure Search
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterList(s.toString())
            }
        })
    }

    // Configure Plus (+) Button
    private fun setupPlusButton() {
        binding.btnPlus.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menu.add(0, 1, 0, "Add Friend")
            popup.menu.add(0, 2, 1, "Create Group")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        findNavController().navigate(R.id.action_messagesFragment_to_addFriend)
                        true
                    }
                    2 -> {
                        findNavController().navigate(R.id.action_messagesFragment_to_createGroup)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    // Observe Data from Firebase
    private fun observeConversations() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModel.conversations.observe(viewLifecycleOwner) { conversations ->
            // 1. Save the original list
            fullConversationList = conversations

            // 2. Display immediately (even if user names haven't finished loading)
            // If a search is active, filter the list; otherwise, show the full list
            val currentQuery = binding.etSearch.text.toString()
            if (currentQuery.isNotEmpty()) {
                filterList(currentQuery)
            } else {
                adapter.submitList(conversations)
            }

            // 3. Process user name loading for 1-on-1 (Single) chats
            conversations.forEachIndexed { index, chat ->
                if (chat.type != "group") {
                    // Find the other person's ID (the ID that is not ours)
                    val otherId = chat.participantIds.find { it != currentUid }

                    if (otherId != null) {
                        if (userCache.containsKey(otherId)) {
                            // If already in cache -> Assign immediately -> Refresh that item
                            chat.otherUser = userCache[otherId]
                            // Only refresh this item if the adapter is showing the full list
                            if (currentQuery.isEmpty()) adapter.notifyItemChanged(index)
                        } else {
                            // If not in cache -> Fetch from API
                            viewModel.getUserInfo(otherId) { user ->
                                if (user != null) {
                                    userCache[otherId] = user
                                    chat.otherUser = user
                                    // Only refresh this item if the adapter is showing the full list
                                    if (currentQuery.isEmpty()) adapter.notifyItemChanged(index)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // List filtering logic
    private fun filterList(query: String) {
        val lowerQuery = query.lowercase().trim()

        val filteredList = fullConversationList.filter { chat ->
            if (chat.type == "group") {
                // If it's a group: search by group name
                chat.groupName.lowercase().contains(lowerQuery)
            } else {
                // If it's a single chat: search by user name (need to null-check as it might not be loaded yet)
                val name = chat.otherUser?.username?.lowercase() ?: ""
                name.contains(lowerQuery)
            }
        }
        adapter.submitList(filteredList)
    }
}
