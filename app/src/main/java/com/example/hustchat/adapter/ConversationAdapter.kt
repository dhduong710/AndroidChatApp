package com.example.hustchat.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hustchat.R
import com.example.hustchat.databinding.ItemConversationBinding
import com.example.hustchat.model.Conversation
import com.example.hustchat.utils.TimeUtils

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

            val imageUrl: String
            val displayName: String

            if (conversation.type == "group") {
                displayName = conversation.groupName.ifEmpty { "Group" }
                // If groupAvatarUrl is null, use the getAvatarUrl function to create an image from the group name
                imageUrl = com.example.hustchat.utils.ImageUtils.getAvatarUrl(displayName, conversation.groupAvatarUrl)
            } else {
                val otherUser = conversation.otherUser
                displayName = otherUser?.username ?: "User"
                imageUrl = com.example.hustchat.utils.ImageUtils.getAvatarUrl(displayName, otherUser?.avatarUrl)
            }

            binding.tvName.text = displayName

            // Use Glide to load the image (It can load both real images and generated ones)
            com.bumptech.glide.Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person) // Placeholder image while loading
                .circleCrop() // Crop the image into a circle for a nice look
                .into(binding.ivAvatar)

            // 2. Handle Last Message
            if (conversation.lastMessage.isEmpty()) {
                binding.tvLastMessage.text = "Start the conversation"
            } else {
                binding.tvLastMessage.text = conversation.lastMessage
            }

            // 3. Handle Timestamp
            binding.tvTimeConv.text = TimeUtils.getTimeDisplay(conversation.timestamp)

            binding.root.setOnClickListener { onClick(conversation) }

            binding.executePendingBindings()
        }
    }
}

class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
    // If the IDs are the same, it's the same item
    override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation) = oldItem.id == newItem.id

    // Check if the contents have changed
    // Must also compare otherUser for DiffUtil to know when to redraw the name
    override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem == newItem && oldItem.otherUser == newItem.otherUser
    }
}
