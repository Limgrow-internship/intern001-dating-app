package com.example.heartondatingapp.presentation.common.viewmodel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.heartondatingapp.presentation.navigation.NavigationManager
import dagger.hilt.android.AndroidEntryPoint
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
