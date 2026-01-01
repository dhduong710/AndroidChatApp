package com.example.hustchat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hustchat.databinding.FragmentRegisterBinding
import com.example.hustchat.viewmodel.AuthState
import com.example.hustchat.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnRegister.setOnClickListener {
            val user = binding.etUsername.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmailReg.text.toString().trim()
            val pass = binding.etPassReg.text.toString().trim()

            if (user.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {

                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simple password rule
            if (pass.length < 6) {

                Toast.makeText(context, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(user, email, phone, pass)
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> binding.progressBarReg.visibility = View.VISIBLE
                is AuthState.Success -> {
                    binding.progressBarReg.visibility = View.GONE

                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack() // Go back to login
                }
                is AuthState.Error -> {
                    binding.progressBarReg.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
