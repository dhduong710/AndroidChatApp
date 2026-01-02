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

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: MessageAdapter

    private var conversationId: String = ""
    private var otherName: String = ""

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
    }
}