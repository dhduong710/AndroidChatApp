package com.example.hustchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hustchat.databinding.ItemFriendRequestBinding
import com.example.hustchat.model.FriendRequest

class RequestAdapter(
    private val onAcceptClick: (FriendRequest) -> Unit
) : ListAdapter<FriendRequest, RequestAdapter.RequestViewHolder>(RequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemFriendRequestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RequestViewHolder(private val binding: ItemFriendRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: FriendRequest) {
            binding.request = request
            binding.executePendingBindings()

            binding.btnAccept.setOnClickListener {
                onAcceptClick(request)
            }
        }
    }
}

class RequestDiffCallback : DiffUtil.ItemCallback<FriendRequest>() {
    override fun areItemsTheSame(oldItem: FriendRequest, newItem: FriendRequest): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FriendRequest, newItem: FriendRequest): Boolean {
        return oldItem == newItem
    }
}