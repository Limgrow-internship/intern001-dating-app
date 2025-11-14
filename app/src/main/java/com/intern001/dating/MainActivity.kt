package com.intern001.dating

import android.os.Bundle
import android.view.View
import com.intern001.dating.databinding.ActivityMainBinding
import com.intern001.dating.presentation.common.viewmodel.BaseActivity
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
        setupNavigation()
        setupBottomNavigation()
        setupDestinationListener()

        if (intent.getBooleanExtra("open_home", false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, HomeFragment())
                .commit()
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
