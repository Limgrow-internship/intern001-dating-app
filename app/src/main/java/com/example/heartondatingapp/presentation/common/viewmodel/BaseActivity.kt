package com.example.heartondatingapp.presentation.common.viewmodel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var navController: NavController

    abstract fun getNavHostFragmentId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupNavigation()
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
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun canNavigateBack(): Boolean {
        return navController.previousBackStackEntry != null
    }

    fun navigateBack() {
        if (canNavigateBack()) {
            navController.navigateUp()
        } else {
            finish()
        }
    }
}
