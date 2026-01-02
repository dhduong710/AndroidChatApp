package com.example.hustchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hustchat.databinding.ItemConversationBinding
import com.example.hustchat.model.Conversation

import com.example.hustchat.R


class ConversationAdapter(
    private val onClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConversationViewHolder(private val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            // Logic for displaying Name
            if (conversation.type == "group") {
                binding.tvName.text = conversation.groupName
                binding.ivAvatar.setImageResource(R.drawable.ic_group)
            } else {
                binding.tvName.text = conversation.otherUser?.username ?: "Loading..."
                binding.ivAvatar.setImageResource(R.drawable.ic_person)
            }

            binding.tvLastMessage.text = conversation.lastMessage

            binding.conversation = conversation
            binding.tvTimeConv.text = com.example.hustchat.utils.TimeUtils.getTimeDisplay(conversation.timestamp)
            binding.executePendingBindings()

            binding.root.setOnClickListener {
                onClick(conversation)
            }
        }
    }
}

class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
    override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem == newItem
    }
}