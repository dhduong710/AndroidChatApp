package com.example.hustchat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hustchat.R
import com.example.hustchat.databinding.ItemMessageReceivedBinding
import com.example.hustchat.databinding.ItemMessageSentBinding
import com.example.hustchat.model.ChatItem
import com.example.hustchat.utils.ImageUtils
import com.example.hustchat.utils.TimeUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatAdapter(
    private val isGroup: Boolean,
    private val partnerName: String?,
    private val partnerAvatarUrl: String?
) : ListAdapter<ChatItem, RecyclerView.ViewHolder>(ChatItemDiffCallback()) {

    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    // Cache stores a pair of (Name, Image URL) for optimization
    private val userCache = HashMap<String, Pair<String, String>>()

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is ChatItem.DateHeader -> TYPE_HEADER
            is ChatItem.MessageItem -> {
                // System messages are also considered received messages
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
            else -> { // TYPE_RECEIVED
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
            binding.message = message
            binding.tvContent.text = message.content
            binding.tvTime.text = TimeUtils.formatTime(message.timestamp)

            // SYSTEM MESSAGES
            if (message.senderId == "SYSTEM") {
                binding.tvSenderName.visibility = View.VISIBLE
                binding.ivAvatar.visibility = View.VISIBLE

                // Set a fixed name for the system
                binding.tvSenderName.text = "System"

                // Use ImageUtils to create an avatar for "System"
                val systemAvatarUrl = ImageUtils.getAvatarUrl("System", null)

                // Use Glide to load the avatar
                Glide.with(binding.root.context)
                    .load(systemAvatarUrl)
                    .placeholder(R.drawable.ic_person) // Placeholder can be a gear or notification icon
                    .circleCrop()
                    .into(binding.ivAvatar)

                binding.executePendingBindings()
                return // Return early, do not run the code below
            }

            // Handle regular messages (from users)
            if (isGroup) {
                binding.tvSenderName.visibility = View.VISIBLE
                binding.ivAvatar.visibility = View.VISIBLE

                val senderId = message.senderId

                if (userCache.containsKey(senderId)) {
                    val (name, finalUrl) = userCache[senderId]!!
                    binding.tvSenderName.text = name
                    Glide.with(binding.root.context).load(finalUrl).placeholder(R.drawable.ic_person).circleCrop().into(binding.ivAvatar)
                } else {
                    binding.tvSenderName.text = "Loading..."
                    FirebaseFirestore.getInstance().collection("users").document(senderId)
                        .get().addOnSuccessListener { doc ->
                            val name = doc.getString("username") ?: "Unknown"
                            val avatarUrl = doc.getString("avatarUrl")
                            val finalUrl = ImageUtils.getAvatarUrl(name, avatarUrl)
                            userCache[senderId] = Pair(name, finalUrl)

                            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                                binding.tvSenderName.text = name
                                Glide.with(binding.root.context).load(finalUrl).placeholder(R.drawable.ic_person).circleCrop().into(binding.ivAvatar)
                            }
                        }
                }
            } else { // 1-on-1 Chat
                binding.tvSenderName.visibility = View.GONE
                binding.ivAvatar.visibility = View.VISIBLE

                val finalPartnerName = partnerName ?: "User"
                val finalUrl = ImageUtils.getAvatarUrl(finalPartnerName, partnerAvatarUrl)

                Glide.with(binding.root.context)
                    .load(finalUrl)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(binding.ivAvatar)
            }

            binding.executePendingBindings()
        }
    }
}

class ChatItemDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem) = oldItem == newItem
}
