package com.example.hustchat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hustchat.R
import com.example.hustchat.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {
    private lateinit var binding: FragmentMessagesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPlus.setOnClickListener { view ->
            showPlusMenu(view)
        }
    }

    private fun showPlusMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)

        popup.menu.add(0, 1, 0, "Add friends")
        popup.menu.add(0, 2, 1, "Create group")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    findNavController().navigate(R.id.action_messages_to_addFriend)
                    true
                }
                2 -> {
                    findNavController().navigate(R.id.action_messages_to_createGroup)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}