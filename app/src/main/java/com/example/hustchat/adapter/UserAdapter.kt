package com.example.hustchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hustchat.databinding.ItemUserSearchBinding
import com.example.hustchat.model.User

class UserAdapter(private val onAddClick: (User) -> Unit) :
    ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserSearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(private val binding: ItemUserSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.user = user
            binding.executePendingBindings()

            // Reset the button state (because RecyclerView reuses Views)
            binding.btnAdd.isEnabled = true
            binding.btnAdd.text = "Add Friend" // TRANSLATED
            binding.btnAdd.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#D71920")
            )

            binding.btnAdd.setOnClickListener {
                onAddClick(user)

                // Provide immediate UI Feedback
                binding.btnAdd.text = "Sent"
                binding.btnAdd.isEnabled = false
                binding.btnAdd.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY)
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}
