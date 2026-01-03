package com.example.hustchat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hustchat.R
import com.example.hustchat.databinding.ItemMessageReceivedBinding
import com.example.hustchat.databinding.ItemMessageSentBinding
import com.example.hustchat.model.ChatItem
import com.example.hustchat.utils.TimeUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatAdapter(private val isGroup: Boolean) :
    ListAdapter<ChatItem, RecyclerView.ViewHolder>(ChatItemDiffCallback()) {

    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    // Cache user names in a group to avoid repeated loads
    private val userCache = HashMap<String, String>()

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is ChatItem.DateHeader -> TYPE_HEADER
            is ChatItem.MessageItem -> {
                if (item.message.senderId == currentUid) TYPE_SENT else TYPE_RECEIVED
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_date_header, parent, false)
                DateViewHolder(view)
            }
            TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(inflater, parent, false)
                SentViewHolder(binding)
            }
            else -> {
                val binding = ItemMessageReceivedBinding.inflate(inflater, parent, false)
                ReceivedViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is DateViewHolder -> holder.bind((item as ChatItem.DateHeader))
            is SentViewHolder -> holder.bind((item as ChatItem.MessageItem).message)
            is ReceivedViewHolder -> holder.bind((item as ChatItem.MessageItem).message)
        }
    }

    // VIEW HOLDERS

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ChatItem.DateHeader) {
            (itemView as TextView).text = TimeUtils.getDateHeaderDisplay(item.timestamp)
        }
    }

    class SentViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: com.example.hustchat.model.Message) {

            binding.message = message

            binding.tvContent.text = message.content
            binding.tvTime.text = TimeUtils.formatTime(message.timestamp)

            binding.executePendingBindings()
        }
    }

    inner class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: com.example.hustchat.model.Message) {
            // Bind data
            binding.message = message
            binding.tvContent.text = message.content
            binding.tvTime.text = TimeUtils.formatTime(message.timestamp)

            // Handle system messages, applies to both group and friend chats
            if (message.senderId == "SYSTEM") {
                binding.tvSenderName.visibility = View.VISIBLE // Show name TextView
                binding.tvSenderName.text = "System"           // Set name to "System"
                binding.ivAvatar.visibility = View.VISIBLE     // Show avatar
                binding.ivAvatar.setImageResource(R.drawable.ic_person) // Set avatar to ic_person

                // End the bind() function here since we are done processing the system message
                binding.executePendingBindings()
                return
            }

            // Handle regular messages (not from system)
            // Logic to display Name and Avatar in a Group Chat
            if (isGroup) {
                binding.tvSenderName.visibility = View.VISIBLE
                binding.ivAvatar.visibility = View.VISIBLE

                val senderId = message.senderId

                // Check cache for the sender's name
                if (userCache.containsKey(senderId)) {
                    binding.tvSenderName.text = userCache[senderId]
                } else {
                    binding.tvSenderName.text = "Loading..."
                    // Load name from Firestore
                    FirebaseFirestore.getInstance().collection("users").document(senderId)
                        .get().addOnSuccessListener { doc ->
                            val name = doc.getString("username") ?: "Unknown"
                            userCache[senderId] = name
                            // Update the UI if the view has not been recycled
                            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                                binding.tvSenderName.text = name
                            }
                        }
                }
                // Set avatar for the sender
                binding.ivAvatar.setImageResource(R.drawable.ic_person)
            } else {
                // In a 1-1 chat, hide the name but still show the avatar
                binding.tvSenderName.visibility = View.GONE
                binding.ivAvatar.visibility = View.VISIBLE
                binding.ivAvatar.setImageResource(R.drawable.ic_person)
            }

            binding.executePendingBindings()
        }
    }
}

class ChatItemDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem) = oldItem == newItem
}
