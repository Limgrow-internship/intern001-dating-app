package com.intern001.dating.presentation.common.viewmodel

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.intern001.dating.presentation.navigation.NavigationManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var navController: NavController

    @Inject
    lateinit var navigationManager: NavigationManager

    abstract fun getNavHostFragmentId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"
        super.attachBaseContext(updateBaseContextLocale(newBase, lang))
    }

    private fun updateBaseContextLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    protected open fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(getNavHostFragmentId()) as? NavHostFragment

        navController = navHostFragment?.navController
            ?: throw IllegalStateException(
                "NavHostFragment not found with ID ${getNavHostFragmentId()}",
            )
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigationManager.navigateUp(navController) || super.onSupportNavigateUp()
    }

    fun canNavigateBack(): Boolean {
        return navController.previousBackStackEntry != null
    }

    fun navigateBack() {
        if (canNavigateBack()) {
            navigationManager.navigateUp(navController)
        } else {
            finish()
        }
    }
}
