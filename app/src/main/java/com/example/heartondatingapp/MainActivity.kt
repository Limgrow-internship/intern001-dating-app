package com.example.heartondatingapp

import android.os.Bundle
import androidx.navigation.ui.setupWithNavController
import com.example.heartondatingapp.databinding.ActivityMainBinding
import com.example.heartondatingapp.presentation.common.viewmodel.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()
    }

    override fun getNavHostFragmentId() = R.id.nav_host_fragment

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setupWithNavController(navController)
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    fun hideBottomNavigation(hide: Boolean) {
        binding.bottomNavigation.animate()
            .translationY(if (hide) binding.bottomNavigation.height.toFloat() else 0f)
            .duration = 300
    }
}
