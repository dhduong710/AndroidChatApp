package com.example.chatapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

class RegisterFragment : Fragment() {

    // Initialize the ViewModel using the 'by viewModels()' Kotlin property delegate.
    // This correctly scopes the ViewModel to this Fragment's lifecycle.
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the XML layout for this fragment.
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View Initialization
        // Find views by their ID from the inflated layout.
        val etUsername = view.findViewById<EditText>(R.id.etRegUsername)
        val etEmail = view.findViewById<EditText>(R.id.etRegEmail)
        val etPassword = view.findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvBackToLogin = view.findViewById<TextView>(R.id.tvBackToLogin)

        // Event Listeners

        // Handle the Register button click.
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            // Delegate the registration logic to the ViewModel.
            viewModel.register(username, email, pass)
        }

        // Handle the "Back to Login" text click.
        tvBackToLogin.setOnClickListener {
            // Navigate back to the previous screen (LoginFragment) on the back stack.
            findNavController().popBackStack()
        }

        // LiveData Observation
        // Observe the ViewModel's LiveData to react to state changes.

        // Observe the registration result.
        viewModel.registerResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "Welcome!", Toast.LENGTH_SHORT).show()
                // On successful registration, navigate the user directly to the home screen.
                findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
            }
        }

        // Observe for any error messages from the ViewModel.
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                // Display the error message to the user in a Toast.
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }

        // Observe the loading state to provide feedback to the user.
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Disable the register button to prevent multiple clicks while processing.
            btnRegister.isEnabled = !isLoading
            // Update the button text to indicate the current state.
            btnRegister.text = if (isLoading) "Creating Account..." else "Register"
        }
    }
}
