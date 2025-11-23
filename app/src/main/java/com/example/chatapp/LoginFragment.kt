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

class LoginFragment : Fragment() {

    // Initialize the ViewModel using the 'by viewModels()' Kotlin property delegate.
    // This ties the ViewModel's lifecycle to this Fragment.
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment.
        // The third parameter 'attachToRoot' is false because the FragmentManager
        // will handle attaching the view to the container.
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View Initialization
        // Find and assign views from the layout.
        // For a more modern and safer approach, use ViewBinding.
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegister = view.findViewById<TextView>(R.id.tvRegister)

        // Business Logic

        // Check if a user is already signed in. If so, navigate directly to the Home screen.
        viewModel.checkCurrentUser()

        // Set up the login button click listener.
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            // Delegate the login logic to the ViewModel.
            viewModel.login(email, pass)
        }

        // Set up the register text click listener for a quick registration.
        tvRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            // For simplicity, use the current input fields for registration.
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                Toast.makeText(context, "Registering...", Toast.LENGTH_SHORT).show()
                viewModel.register(email, pass)
            } else {
                Toast.makeText(context, "Enter Email & Password to Register", Toast.LENGTH_SHORT).show()
            }
        }

        // LiveData Observation
        // Observe changes in the data provided by the ViewModel.

        // Observe the login result.
        viewModel.loginResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                // On successful login, navigate to the HomeFragment.
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }

        // Observe for any error messages.
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                // Display errors to the user in a Toast message.
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe the loading state to provide user feedback.
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Disable the login button to prevent multiple clicks while an operation is in progress.
            btnLogin.isEnabled = !isLoading
            // Change the button text to indicate the loading state.
            btnLogin.text = if (isLoading) "Loading..." else getString(R.string.login_button)
        }
    }
}
