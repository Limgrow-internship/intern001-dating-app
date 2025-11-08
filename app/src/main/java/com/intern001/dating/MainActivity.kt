package com.intern001.dating

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.navigation.ui.setupWithNavController
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
        binding.bottomNavigation.setupWithNavController(navController)
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    fun hideBottomNavigation(hide: Boolean) {
        binding.bottomNavigation.visibility = if (hide) View.GONE else View.VISIBLE
    }
}
