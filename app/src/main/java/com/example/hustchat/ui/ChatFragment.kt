package com.example.hustchat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hustchat.adapter.MessageAdapter
import com.example.hustchat.databinding.FragmentChatBinding
import com.example.hustchat.viewmodel.MainViewModel

import android.graphics.Color
import com.google.firebase.firestore.FirebaseFirestore
import com.example.hustchat.utils.TimeUtils
import com.example.hustchat.model.User

import com.google.firebase.auth.FirebaseAuth

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: MessageAdapter

    private var conversationId: String = ""
    private var otherName: String = ""

    private var chatName: String = "" // Display name (Friend name or Group name)
    private var isGroup: Boolean = false // The flag indicates whether this is a Group or a chat with a friend.

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Receive data from the MessagesFragment
        arguments?.let {
            conversationId = it.getString("conversationId", "")
            otherName = it.getString("otherName", "Chat")
        }

        binding.tvTitleName.text = otherName
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        // Setup RecyclerView
        adapter = MessageAdapter()
        binding.rvMessages.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true // Always scroll to the bottom.
        }
        binding.rvMessages.adapter = adapter

        // Send message
        binding.btnSend.setOnClickListener {
            val content = binding.etMessage.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.sendMessage(conversationId, content)
                binding.etMessage.setText("")
            }
        }

        // Listen for new messages
        if (conversationId.isNotEmpty()) {
            viewModel.getMessages(conversationId).observe(viewLifecycleOwner) { messages ->
                adapter.submitList(messages) {
                    // When there is a new message, it will automatically scroll to the bottom.
                    if (messages.isNotEmpty()) {
                        binding.rvMessages.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        checkConversationType()
    }
    private fun checkConversationType() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("conversations")
            .document(conversationId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val type = doc.getString("type") ?: "single"

                    if (type == "group") {
                        // GROUP
                        isGroup = true

                        // 1. Hide online status.
                        binding.tvStatus.visibility = View.GONE

                        // 2. Update the group name
                        val groupName = doc.getString("groupName") ?: chatName
                        binding.tvTitleName.text = groupName

                    } else {
                        // Chat with a friend
                        isGroup = false

                        // 1. Find ID of friend
                        val participants = doc.get("participantIds") as? List<String>
                        val otherId = participants?.find { it != currentUid }

                        // 2. Listen to status
                        if (otherId != null) {
                            listenToUserStatus(otherId)
                        }
                    }
                }
            }
    }

    // Chat with friend
    private fun listenToUserStatus(uid: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener

                // If the Fragment has been destroyed (user exited), do not update the UI again to avoid crashing.
                if (!isAdded) return@addSnapshotListener

                val user = value.toObject(User::class.java) ?: return@addSnapshotListener

                binding.tvStatus.visibility = View.VISIBLE
                if (user.status == "online") {
                    binding.tvStatus.text = "Active now"
                    binding.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                } else {
                    binding.tvStatus.text = TimeUtils.getTimeAgo(user.lastSeen)
                    binding.tvStatus.setTextColor(Color.GRAY)
                }
            }
    }
}