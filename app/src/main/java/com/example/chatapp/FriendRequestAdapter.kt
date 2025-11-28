package com.example.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.model.FriendRequest

/**
 * An adapter for the RecyclerView that displays a list of incoming friend requests.
 * This class is responsible for creating and binding the views for each request item.
 */
class FriendRequestAdapter : RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder>() {

    // A private list to hold the friend request data that the adapter will display.
    private val requestList = ArrayList<FriendRequest>()

    /**
     * A lambda function that will be invoked when the "Accept" button
     * on an item is clicked. The Fragment will set this property to define
     * the action to be taken (e.g., calling a ViewModel function).
     */
    var onAcceptClick: ((FriendRequest) -> Unit)? = null

    /**
     * Updates the list of friend requests in the adapter.
     * It clears the existing list, adds the new requests, and notifies the
     * RecyclerView that the data set has changed so it can redraw itself.
     * @param requests The new list of friend requests to display.
     */
    fun setRequests(requests: List<FriendRequest>) {
        requestList.clear()
        requestList.addAll(requests)
        // Notifies any registered observers that the data set has changed.
        notifyDataSetChanged()
    }

    /**
     * Called when the RecyclerView needs a new ViewHolder of the given type to represent an item.
     * This inflates the layout for a single friend request item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        // Inflate the XML layout for a single friend request item (item_friend_request.xml).
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_request, parent, false)
        // Create and return a new RequestViewHolder instance with the inflated view.
        return RequestViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the ViewHolder to reflect the item at the given position.
     */
    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        // Get the FriendRequest object for the current position.
        val request = requestList[position]
        // Bind the request data (sender's name and email) to the views in the ViewHolder.
        holder.tvName.text = request.senderName
        holder.tvEmail.text = request.senderEmail

        // Set up the click listener for the "Accept" button.
        holder.btnAccept.setOnClickListener {
            // When the button is clicked, invoke the onAcceptClick lambda,
            // passing the corresponding FriendRequest object.
            onAcceptClick?.invoke(request)
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    override fun getItemCount(): Int = requestList.size

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * It holds the references to the individual views within the item layout, which improves performance
     * by avoiding repeated calls to findViewById.
     */
    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Find and store references to the UI components in the item_friend_request.xml layout.
        val tvName: TextView = itemView.findViewById(R.id.tvSenderName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvSenderEmail)
        val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
    }
}
