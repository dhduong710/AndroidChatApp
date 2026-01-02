package com.example.hustchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hustchat.databinding.ItemUserCheckboxBinding
import com.example.hustchat.model.User

class UserSelectAdapter(private val users: List<User>) :
    RecyclerView.Adapter<UserSelectAdapter.ViewHolder>() {

    // Save the selected users
    val selectedUsers = HashSet<User>()

    inner class ViewHolder(val binding: ItemUserCheckboxBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.binding.user = user

        // Avoid view reuse errors: Delete the old listener before setting the state.
        holder.binding.cbSelect.setOnCheckedChangeListener(null)
        holder.binding.cbSelect.isChecked = selectedUsers.contains(user)

        // Listen for event check
        holder.binding.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedUsers.add(user)
            else selectedUsers.remove(user)
        }
    }

    override fun getItemCount() = users.size
}