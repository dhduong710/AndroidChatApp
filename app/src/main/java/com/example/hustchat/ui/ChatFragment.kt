package com.example.hustchat.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hustchat.adapter.ChatAdapter
import com.example.hustchat.databinding.FragmentChatBinding
import com.example.hustchat.model.ChatItem
import com.example.hustchat.model.User
import com.example.hustchat.utils.TimeUtils
import com.example.hustchat.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import android.widget.PopupMenu
import android.widget.Toast


class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    private var conversationId: String = ""
    private var chatName: String = ""
    private var partnerAvatarUrl: String? = null

    private var currentChatItems: List<ChatItem> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Get data from arguments
        arguments?.let {
            conversationId = it.getString("conversationId", "")
            chatName = it.getString("otherName", "Chat")
            partnerAvatarUrl = it.getString("avatarUrl")
        }

        // 2. Setup basic UI
        binding.tvTitleName.text = chatName
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        // 3. Setup RecyclerView
        adapter = ChatAdapter(
            isGroup = false,
            partnerName = chatName,
            partnerAvatarUrl = partnerAvatarUrl
        )
        binding.rvMessages.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = adapter

        // 4. Send a message
        binding.btnSend.setOnClickListener {
            val content = binding.etMessage.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.sendMessage(conversationId, content)
                binding.etMessage.setText("")
            }
        }

        // 5. Listen for messages
        if (conversationId.isNotEmpty()) {
            viewModel.getMessages(conversationId).observe(viewLifecycleOwner) { messages ->
                val chatItems = mutableListOf<ChatItem>()
                var lastTimestamp: Long = 0

                messages.forEach { msg ->
                    if (!TimeUtils.isSameDay(msg.timestamp, lastTimestamp)) {
                        chatItems.add(ChatItem.DateHeader(msg.timestamp))
                        lastTimestamp = msg.timestamp
                    }
                    chatItems.add(ChatItem.MessageItem(msg))
                }

                currentChatItems = chatItems

                adapter.submitList(chatItems) {
                    if (chatItems.isNotEmpty()) {
                        binding.rvMessages.smoothScrollToPosition(chatItems.size - 1)
                    }
                }
            }

            // 6. Check conversation type
            checkConversationType()
        }
    }

    private fun checkConversationType() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("conversations")
            .document(conversationId)
            .get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener

                val type = doc.getString("type") ?: "single"

                if (type == "group") {
                    binding.tvStatus.visibility = View.GONE

                    val groupName = doc.getString("groupName") ?: chatName
                    binding.tvTitleName.text = groupName

                    // Adapter for group chat
                    adapter = ChatAdapter(
                        isGroup = true,
                        partnerName = null, // Not needed for group chat
                        partnerAvatarUrl = null
                    )
                    binding.rvMessages.adapter = adapter

                    if (currentChatItems.isNotEmpty()) {
                        adapter.submitList(currentChatItems)
                    }

                    binding.btnSettings.visibility = View.VISIBLE
                    binding.btnSettings.setOnClickListener {
                        showGroupSettingsDialog()
                    }

                } else {
                    val participants = doc.get("participantIds") as? List<String>
                    val otherId = participants?.find { it != currentUid }
                    if (otherId != null) {
                        listenToUserStatus(otherId)
                    }
                    binding.btnSettings.visibility = View.GONE
                }
            }
    }

    private fun listenToUserStatus(uid: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .addSnapshotListener { value, error ->
                if (error != null || value == null || _binding == null) return@addSnapshotListener

                val user = value.toObject(User::class.java) ?: return@addSnapshotListener

                binding.tvStatus.visibility = View.VISIBLE

                val isStatusOnline = user.status == "online"
                val currentTime = System.currentTimeMillis()
                val isRecent = (currentTime - user.lastSeen) < 1 * 60 * 1000 // 1 minute

                if (isStatusOnline && isRecent) {
                    binding.tvStatus.text = "Active now"
                    binding.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                } else {
                    binding.tvStatus.text = TimeUtils.getTimeAgo(user.lastSeen)
                    binding.tvStatus.setTextColor(Color.GRAY)
                }
            }
    }

    // Function to show the settings dialog
    private fun showGroupSettingsDialog() {
        val popup = PopupMenu(requireContext(), binding.btnSettings)
        popup.menu.add(0, 1, 0, "Change group name")
        popup.menu.add(0, 2, 1, "Change group photo") // Placeholder

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    showRenameDialog()
                    true
                }
                2 -> {
                    Toast.makeText(context, "Feature not available", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showRenameDialog() {
        val input = android.widget.EditText(requireContext())
        input.hint = "Enter new group name"

        input.setText(binding.tvTitleName.text)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Change group name")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.updateGroupProfile(conversationId, newName)
                    binding.tvTitleName.text = newName
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
