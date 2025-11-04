package com.example.heartondatingapp.presentation.common.viewmodel

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.heartondatingapp.presentation.navigation.NavigationManager

abstract class BaseFragment : Fragment() {

    protected val navigationManager: NavigationManager
        get() = (activity as? BaseActivity)?.navigationManager
            ?: throw IllegalStateException("Activity must extend BaseActivity")

    protected val navController: NavController
        get() = findNavController()

    protected fun navigateUp() {
        navigationManager.navigateUp(navController)
    }

    protected fun navigateToRoute(route: String) {
        navigationManager.navigateToDynamicRoute(navController, route)
    }

    protected fun canNavigateBack(): Boolean {
        return navController.previousBackStackEntry != null
    }
}
