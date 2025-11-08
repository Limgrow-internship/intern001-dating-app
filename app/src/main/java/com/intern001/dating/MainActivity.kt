package com.intern001.dating

import android.content.Context
import android.os.Bundle
import android.view.View
import com.intern001.dating.databinding.ActivityMainBinding
import com.intern001.dating.presentation.common.viewmodel.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en")
        setAppLocale(lang ?: "en")

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
        setupBottomNavigation()
        setupDestinationListener()
    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
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
            val showBottomNavDestinations = setOf(
                R.id.homeFragment,
                R.id.chatListFragment,
                R.id.notificationFragment,
                R.id.profileFragment,
            )

            val shouldShow = showBottomNavDestinations.contains(destination.id)
            binding.bottomNavigation.visibility = if (shouldShow) View.VISIBLE else View.GONE

            if (shouldShow) {
                binding.bottomNavigation.selectedItemId = destination.id
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
