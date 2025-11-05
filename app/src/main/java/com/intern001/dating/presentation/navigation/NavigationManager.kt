package com.intern001.dating.presentation.navigation

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class NavigationManager @Inject constructor() {
    fun navigate(
        navController: NavController,
        directions: NavDirections,
        navOptions: NavOptions? = null,
    ) {
        try {
            if (navOptions != null) {
                navController.navigate(directions, navOptions)
            } else {
                navController.navigate(directions)
            }
        } catch (e: Exception) {
            Log.e("NavigationManager", "Navigation failed: ${e.message}")
        }
    }

    fun navigateToDynamicRoute(
        navController: NavController,
        route: String,
        navOptions: NavOptions? = null,
    ) {
        try {
            navController.navigate(route, navOptions)
        } catch (e: Exception) {
            Log.e("NavigationManager", "Navigation failed: ${e.message}")
        }
    }

    fun navigateUp(navController: NavController): Boolean {
        return navController.navigateUp()
    }

    fun navigateToDeepLink(navController: NavController, uri: Uri) {
        try {
            val request = NavDeepLinkRequest.Builder
                .fromUri(uri)
                .build()
            navController.navigate(request)
        } catch (e: Exception) {
            Log.e("NavigationManager", "Navigation failed: ${e.message}")
        }
    }

    fun handleDeepLink(intent: Intent): Uri? {
        return intent.data
    }

    fun getCurrentDestination(navController: NavController): String? {
        return navController.currentDestination?.route
    }

    fun popBackStackTo(
        navController: NavController,
        route: String,
        inclusive: Boolean = false,
    ): Boolean {
        return navController.popBackStack(route, inclusive)
    }

    fun canNavigate(navController: NavController): Boolean {
        return navController.currentDestination != null
    }

    fun navigateAndClearBackStack(navController: NavController, route: String) {
        try {
            navController.navigate(route) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        } catch (e: Exception) {
            Log.e("NavigationManager", "Navigation failed: ${e.message}")
        }
    }
}
