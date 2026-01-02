package com.example.hustchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.hustchat.databinding.ItemMessageReceivedBinding
import com.example.hustchat.databinding.ItemMessageSentBinding
import com.example.hustchat.model.Message
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == currentUid) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(inflater, parent, false)
            SentViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(inflater, parent, false)
            ReceivedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is SentViewHolder) holder.bind(message)
        else if (holder is ReceivedViewHolder) holder.bind(message)
    }

    class SentViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.message = message
            binding.tvTime.text = com.example.hustchat.utils.TimeUtils.formatTime(message.timestamp)
            binding.executePendingBindings()
        }
    }

    class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.message = message
            binding.tvTime.text = com.example.hustchat.utils.TimeUtils.formatTime(message.timestamp)
            binding.executePendingBindings()
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem
}