package com.intern001.dating.presentation.navigation

sealed class NavigationEvent {
    // Profile Navigation
    data class NavigateToProfileDetail(val userId: String) : NavigationEvent()
    data class NavigateToProfileDetailWithAction(
        val userId: String,
        val actionType: String,
    ) : NavigationEvent()

    // Chat Navigation
    data class NavigateToChatDetail(val userId: String) : NavigationEvent()
    data class NavigateToChatDetailWithId(val chatId: String) : NavigationEvent()

    // Auth Navigation
    object NavigateToLogin : NavigationEvent()
    object NavigateToSignup : NavigationEvent()
    object NavigateToForgotPassword : NavigationEvent()

    // Bottom Tab Navigation
    object NavigateToHome : NavigationEvent()
    object NavigateToChat : NavigationEvent()
    object NavigateToNotification : NavigationEvent()
    object NavigateToProfile : NavigationEvent()

    // Edit & Settings
    object NavigateToEditProfile : NavigationEvent()
    object NavigateToSettings : NavigationEvent()

    // Deep Link Navigation
    data class NavigateToDeepLink(val uri: String) : NavigationEvent()

    // Back Navigation
    object NavigateUp : NavigationEvent()
    object NavigateBack : NavigationEvent()

    // Custom Navigation
    data class NavigateToRoute(val route: String) : NavigationEvent()

    // Idle state
    object Idle : NavigationEvent()
}
