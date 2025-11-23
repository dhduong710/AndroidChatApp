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
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegister = view.findViewById<TextView>(R.id.tvRegister)

        // Business Logic
        viewModel.checkCurrentUser()

        // Set up the login button click listener.
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            viewModel.login(email, pass)
        }

        // Set up the "Register now" text click listener to navigate to the RegisterFragment.
        tvRegister.setOnClickListener {
            // Use the NavController to perform the navigation action defined in nav_graph.xml.
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // LiveData Observation
        viewModel.loginResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            btnLogin.isEnabled = !isLoading
            btnLogin.text = if (isLoading) "Loading..." else getString(R.string.login_button)
        }
    }
}
