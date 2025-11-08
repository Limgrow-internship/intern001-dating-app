package com.intern001.dating

import android.os.Bundle
import android.view.View
import com.intern001.dating.R
import com.intern001.dating.databinding.ActivityMainBinding
import com.intern001.dating.presentation.common.viewmodel.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
        setupBottomNavigation()
        setupDestinationListener()
    }

    override fun getNavHostFragmentId() = R.id.nav_host_fragment

    private fun setupBottomNavigation() {
        binding.bottomNavigation.visibility = View.GONE

        binding.bottomNavigation.setOnItemSelectedListener { item ->
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
            // List of destinations where bottom nav should be visible
            val showBottomNavDestinations = setOf(
                R.id.homeFragment,
                R.id.chatListFragment,
                R.id.notificationFragment,
                R.id.profileFragment,
            )

            val shouldShow = showBottomNavDestinations.contains(destination.id)

            // Update bottom navigation visibility
            binding.bottomNavigation.visibility = if (shouldShow) View.VISIBLE else View.GONE

            // Update selected item if we're on a bottom nav destination
            if (shouldShow) {
                binding.bottomNavigation.selectedItemId = destination.id
            }

            // Debug: Print destination info
            android.util.Log.d("MainActivity", "Destination: ${destination.label} (ID: ${destination.id}), Show bottom nav: $shouldShow, Current visibility: ${binding.bottomNavigation.visibility}")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun hideBottomNavigation(hide: Boolean) {
        binding.bottomNavigation.visibility = if (hide) View.GONE else View.VISIBLE
    }
}
