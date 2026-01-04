package com.example.hustchat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hustchat.databinding.FragmentEditProfileBinding
import com.example.hustchat.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the current username into the EditText
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener {
                    binding.etUsername.setText(it.getString("username"))
                }
        }

        binding.btnChangeAvatar.setOnClickListener {
            Toast.makeText(context, "Feature in development (Requires Firebase Storage)", Toast.LENGTH_SHORT).show()
        }

        binding.btnSave.setOnClickListener {
            val newName = binding.etUsername.text.toString().trim()
            if (newName.isNotEmpty()) {
                viewModel.updateUserProfile(newName)
                findNavController().popBackStack()
            } else {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
