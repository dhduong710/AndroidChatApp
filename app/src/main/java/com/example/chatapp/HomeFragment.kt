package com.example.chatapp

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.chatapp.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    // Using view binding to safely access views. _binding is nullable.
    private var _binding: FragmentHomeBinding? = null
    // This non-nullable property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using view binding.
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the Logout Menu on the Toolbar.
        setupMenu()

        // By default, display the ChatListFragment when the screen is first opened.
        if (savedInstanceState == null) {
            replaceFragment(ChatListFragment())
        }

        // Handle item selection events for the Bottom Navigation View.
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    replaceFragment(ChatListFragment())
                    true // Return true to show the item as selected.
                }
                R.id.nav_contact -> {
                    replaceFragment(ContactFragment())
                    true // Return true to show the item as selected.
                }
                else -> false
            }
        }
    }

    /**
     * Replaces the current child fragment inside the 'home_content_container'.
     * @param fragment The new fragment to display.
     */
    private fun replaceFragment(fragment: Fragment) {
        // Use childFragmentManager because this is a nested fragment scenario.
        // The HomeFragment is hosting other fragments (ChatListFragment, ContactFragment).
        childFragmentManager.beginTransaction()
            .replace(R.id.home_content_container, fragment)
            .commit()
    }

    /**
     * Sets up the toolbar menu, specifically for the logout action.
     */
    private fun setupMenu() {
        // Add a MenuProvider to handle the creation and selection of menu items.
        // This is the modern and lifecycle-aware way to manage menus in fragments.
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Inflate the custom menu layout file.
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle menu item clicks.
                return when (menuItem.itemId) {
                    R.id.action_logout -> {
                        // Sign the user out from Firebase.
                        FirebaseAuth.getInstance().signOut()
                        // Navigate back to the login screen, clearing the back stack.
                        findNavController().navigate(R.id.loginFragment)
                        true // Indicate that the event was handled.
                    }
                    else -> false // Let other components handle the event.
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * Clean up the binding instance when the view is destroyed to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
