package com.intern001.dating.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

object NavGraph {
    const val ROUTE_HOME = "home"
    const val ROUTE_CHAT = "chat"
    const val ROUTE_NOTIFICATION = "notification"
    const val ROUTE_PROFILE = "profile"

    const val ROUTE_FOR_YOU = "for_you"
    const val ROUTE_LIKED_YOU = "liked_you"
    const val ROUTE_VIEWED_YOU = "viewed_you"

    const val ROUTE_PROFILE_DETAIL = "profile_detail"
    const val ROUTE_CHAT_DETAIL = "chat_detail"
    const val ROUTE_EDIT_PROFILE = "edit_profile"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_DATING_MODE = "dating_mode"

    const val ROUTE_SPLASH = "splash"
    const val ROUTE_LOGIN = "login"
    const val ROUTE_SIGNUP = "signup"
    const val ROUTE_FORGOT_PASSWORD = "forgot_password"

    const val ARG_USER_ID = "userId"
    const val ARG_CHAT_ID = "chatId"
    const val ARG_ACTION_TYPE = "actionType"
}

fun NavController.navigateToHome() {
    navigate(NavGraph.ROUTE_HOME) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.navigateToChat() {
    navigate(NavGraph.ROUTE_CHAT) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.navigateToNotification() {
    navigate(NavGraph.ROUTE_NOTIFICATION) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.navigateToProfile() {
    navigate(NavGraph.ROUTE_PROFILE) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.navigateToProfileDetail(userId: String) {
    val route = "${NavGraph.ROUTE_PROFILE_DETAIL}/$userId"
    navigate(route)
}

fun NavController.navigateToChatDetail(userId: String) {
    val route = "${NavGraph.ROUTE_CHAT_DETAIL}/$userId"
    navigate(route)
}

fun NavController.navigateToDatingMode(
    likerId: String? = null,
    targetUserId: String? = null,
    targetListUserId: String? = null,
    allowMatchedProfile: Boolean = false
) {
    val route = "${NavGraph.ROUTE_DATING_MODE}?" +
            "likerId=${likerId ?: ""}&" +
            "targetUserId=${targetUserId ?: ""}&" +
            "targetListUserId=${targetListUserId ?: ""}&" +
            "allowMatchedProfile=$allowMatchedProfile"
    navigate(route)
}

fun NavController.navigateToEditProfile() {
    navigate(NavGraph.ROUTE_EDIT_PROFILE)
}

fun NavController.navigateToSettings() {
    navigate(NavGraph.ROUTE_SETTINGS)
}

fun NavController.navigateToLogin() {
    navigate(NavGraph.ROUTE_LOGIN) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

fun NavController.navigateToSignup() {
    navigate(NavGraph.ROUTE_SIGNUP)
}

fun NavController.navigateToForgotPassword() {
    navigate(NavGraph.ROUTE_FORGOT_PASSWORD)
}

fun NavController.popBackStackTo(route: String, inclusive: Boolean = false): Boolean {
    return popBackStack(route, inclusive)
}

fun NavController.getCurrentRoute(): String? {
    return currentDestination?.route
}
