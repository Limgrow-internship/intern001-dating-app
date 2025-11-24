package com.intern001.dating

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.intern001.dating.databinding.ActivityMainBinding
import com.intern001.dating.presentation.common.viewmodel.BaseActivity
import com.intern001.dating.presentation.navigation.navigateToChatDetail
import com.intern001.dating.presentation.navigation.navigateToNotification
import com.intern001.dating.presentation.navigation.navigateToProfileDetail
import com.intern001.dating.presentation.ui.home.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isNavigatingProgrammatically = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        setupNavigation()
        setupBottomNavigation()
        setupDestinationListener()

        // Handle notification click
        handleNotificationIntent(intent)

        if (intent.getBooleanExtra("open_home", false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, HomeFragment())
                .commit()
        }
    }

    /**
     * Request notification permission for Android 13+ (API 33+)
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "✅ Notification permission already granted")
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) -> {
                    // Show explanation dialog if needed
                    Log.d("MainActivity", "Should show rationale for notification permission")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1001,
                    )
                }
                else -> {
                    // Request permission
                    Log.d("MainActivity", "Requesting notification permission")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1001,
                    )
                }
            }
        } else {
            Log.d("MainActivity", "Android version < 13, notification permission not required")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "✅ Notification permission granted")
                } else {
                    Log.w("MainActivity", "❌ Notification permission denied")
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    /**
     * Handle notification click and navigate to appropriate screen
     */
    private fun handleNotificationIntent(intent: Intent?) {
        val notificationType = intent?.getStringExtra("notification_type")
        val navigateTo = intent?.getStringExtra("navigate_to")

        if (notificationType.isNullOrEmpty()) {
            return // Not a notification intent
        }

        Log.d("MainActivity", "Handling notification: type=$notificationType, navigateTo=$navigateTo")

        when (notificationType) {
            "like" -> {
                val likerId = intent?.getStringExtra("likerId")
                when (navigateTo) {
                    "notification" -> {
                        // Navigate to notification screen to see all likes
                        navController.navigateToNotification()
                    }
                    "profile" -> {
                        // Navigate to liker's profile
                        likerId?.let {
                            navController.navigateToProfileDetail(it)
                        } ?: run {
                            // Fallback to notification screen if no likerId
                            navController.navigateToNotification()
                        }
                    }
                    else -> {
                        // Default: navigate to notification screen
                        navController.navigateToNotification()
                    }
                }
            }
            "match" -> {
                val matchId = intent?.getStringExtra("matchId")
                val matchedUserId = intent?.getStringExtra("matchedUserId")
                when (navigateTo) {
                    "chat" -> {
                        // Navigate to chat with matched user
                        matchedUserId?.let {
                            navController.navigateToChatDetail(it)
                        } ?: run {
                            // Fallback to chat list if no userId
                            navController.navigate(R.id.chatListFragment)
                        }
                    }
                    "match_detail" -> {
                        // Navigate to match detail screen (if exists)
                        // For now, navigate to chat
                        matchedUserId?.let {
                            navController.navigateToChatDetail(it)
                        } ?: run {
                            navController.navigate(R.id.chatListFragment)
                        }
                    }
                    else -> {
                        // Default: navigate to chat with matched user
                        matchedUserId?.let {
                            navController.navigateToChatDetail(it)
                        } ?: run {
                            navController.navigate(R.id.chatListFragment)
                        }
                    }
                }
            }
        }
    }

    override fun getNavHostFragmentId() = R.id.nav_host_fragment

    private fun setupBottomNavigation() {
        binding.bottomNavigation.visibility = View.GONE

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (isNavigatingProgrammatically) {
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.homeFragment,
                R.id.chatListFragment,
                R.id.notificationFragment,
                R.id.profileFragment,
                -> {
                    navController.navigate(item.itemId)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDestinationListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showBottomNavDestinations = setOf(
                R.id.homeFragment,
                R.id.chatListFragment,
                R.id.notificationFragment,
                R.id.profileFragment,
            )

            val shouldShow = showBottomNavDestinations.contains(destination.id)
            binding.bottomNavigation.visibility = if (shouldShow) View.VISIBLE else View.GONE

            if (shouldShow) {
                isNavigatingProgrammatically = true
                binding.bottomNavigation.selectedItemId = destination.id
                isNavigatingProgrammatically = false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun hideBottomNavigation(hide: Boolean) {
        binding.bottomNavigation.visibility = if (hide) View.GONE else View.VISIBLE
    }
}
