package com.example.hustchat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.hustchat.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Handler to run the online status update heartbeat
    private val handler = Handler(Looper.getMainLooper())

    // Flag to check if the current screen is a Login/Register screen
    private var isAuthScreen = false

    private val heartbeatRunnable = object : Runnable {
        override fun run() {
            // Only update if NOT on an Auth screen and the user is logged in
            if (!isAuthScreen && FirebaseAuth.getInstance().currentUser != null) {
                updateStatus("online")
            }
            // Repeat every 60 seconds
            handler.postDelayed(this, 60000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup Navigation Controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 2. CONNECT BOTTOM NAV WITH CONTROLLER
        binding.bottomNav.setupWithNavController(navController)

        // 3. Listen for destination changes to Hide/Show BottomNav and set status
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // Screens that need to HIDE BottomNav & Set Offline
                R.id.loginFragment, R.id.registerFragment -> {
                    isAuthScreen = true
                    updateStatus("offline")
                    binding.bottomNav.visibility = View.GONE
                }
                // FUNCTIONAL screens (Chat, AddFriend...) -> Remain Online
                R.id.chatFragment, R.id.addFriendFragment, R.id.createGroupFragment -> {
                    isAuthScreen = false
                    updateStatus("online")
                    //binding.bottomNav.visibility = View.GONE
                }
                // MAIN screens (Messages, Me) -> SHOW BottomNav & Online
                else -> {
                    isAuthScreen = false
                    updateStatus("online")
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Start the heartbeat
        handler.post(heartbeatRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Stop the heartbeat and set status to offline immediately
        handler.removeCallbacks(heartbeatRunnable)
        updateStatus("offline")
    }

    private fun updateStatus(status: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // If on an Auth screen, block setting status to "online"
        if (isAuthScreen && status == "online") return

        val updates = hashMapOf<String, Any>(
            "status" to status,
            "lastSeen" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("users").document(uid).update(updates)
    }
}
