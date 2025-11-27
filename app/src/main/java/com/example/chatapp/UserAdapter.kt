package com.example.chatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.model.User

/**
 * An adapter for the RecyclerView that displays a list of users.
 * This class is responsible for creating and binding views for each user item.
 */
class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // A private list to hold the user data that the adapter will display.
    private val userList = ArrayList<User>()

    /**
     * A lambda function that will be invoked when the "Add Friend" button
     * on an item is clicked. The Fragment will set this property to define
     * the action to be taken.
     */
    var onAddFriendClick: ((User) -> Unit)? = null

    /**
     * Updates the list of users in the adapter.
     * It clears the existing list, adds the new users, and notifies the
     * RecyclerView that the data has changed so it can redraw itself.
     * @param users The new list of users to display.
     */
    fun setUsers(users: List<User>) {
        userList.clear()
        userList.addAll(users)
        // Notifies any registered observers that the data set has changed.
        notifyDataSetChanged()
    }

    /**
     * Called when the RecyclerView needs a new ViewHolder of the given type to represent an item.
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        // Inflate the XML layout for a single user item (item_user.xml).
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        // Create and return a new UserViewHolder instance with the inflated view.
        return UserViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the ViewHolder to reflect the item at the given position.
     */
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        // Get the User object for the current position.
        val user = userList[position]
        // Bind the user's data to the views inside the ViewHolder.
        holder.bind(user)

        // Set up the click listener for the "Add Friend" button.
        holder.btnAddFriend.setOnClickListener {
            // When the button is clicked, invoke the onAddFriendClick lambda,
            // passing the corresponding user object.
            onAddFriendClick?.invoke(user)
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    override fun getItemCount(): Int = userList.size

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * It holds the references to the individual views within the item layout.
     */
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Find and store references to the UI components in the item_user.xml layout.
        // Using `findViewById` is necessary here as this is not using view binding directly.
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val btnAddFriend: ImageButton = itemView.findViewById(R.id.btnAddFriend)

        /**
         * A helper function to bind a User object to the views.
         * This keeps the onBindViewHolder method cleaner.
         */
        fun bind(user: User) {
            tvUsername.text = user.username
            tvEmail.text = user.email
        }
    }
}
