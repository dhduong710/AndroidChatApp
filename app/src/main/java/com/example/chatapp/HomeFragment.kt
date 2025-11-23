package com.example.chatapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    /**
     * Called by the system to create and return the view hierarchy associated with the fragment.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return Returns the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflates (creates) the view from the XML layout file 'fragment_home'.
        //
        // @param attachToRoot: false
        // This parameter is set to false because the FragmentManager is responsible for handling
        // the attachment of this fragment's view to the parent container (the 'container' ViewGroup).
        // If this were set to true, it would cause a crash because the view would be attached twice.
        // The system automatically handles adding the view to the container at the appropriate time.
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored into the view.
     * This is a good place to set up event listeners and perform other view-related initializations.
     * @param view The View returned by onCreateView().
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            // Sign the user out of their Firebase account.
            FirebaseAuth.getInstance().signOut()

            // After logging out, navigate back to the LoginFragment.
            // Using an action with popUpTo and popUpToInclusive ensures the back stack is cleared,
            // so the user cannot press the back button to return to the HomeFragment.
            findNavController().navigate(R.id.loginFragment)
        }
    }
}
