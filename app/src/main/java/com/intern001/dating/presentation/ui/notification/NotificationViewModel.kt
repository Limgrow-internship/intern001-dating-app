package com.intern001.dating.presentation.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.common.notification.resolveTargetUserId
import com.intern001.dating.domain.model.Notification
import com.intern001.dating.domain.usecase.notification.DeleteAllNotificationsUseCase
import com.intern001.dating.domain.usecase.notification.DeleteNotificationUseCase
import com.intern001.dating.domain.usecase.notification.GetNotificationsFlowUseCase
import com.intern001.dating.domain.usecase.notification.GetNotificationsUseCase
import com.intern001.dating.domain.usecase.notification.GetUnreadNotificationCountUseCase
import com.intern001.dating.domain.usecase.notification.MarkAllNotificationsAsReadUseCase
import com.intern001.dating.domain.usecase.notification.MarkNotificationAsReadUseCase
import com.intern001.dating.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val getNotificationsFlowUseCase: GetNotificationsFlowUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val markAllNotificationsAsReadUseCase: MarkAllNotificationsAsReadUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase,
    private val deleteAllNotificationsUseCase: DeleteAllNotificationsUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Notification>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Notification>>> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var isInitialLoad = true

    init {
        loadNotifications()
        observeNotificationsFlow()
    }

    /**
     * Initial load - show loading state
     */
    private fun loadNotifications() {
        if (isInitialLoad) {
            viewModelScope.launch {
                _uiState.value = UiState.Loading
                getNotificationsUseCase()
                    .onSuccess { notifications ->
                        // Filter notifications by current user
                        val filteredNotifications = filterNotificationsByCurrentUser(notifications)
                        _uiState.value = UiState.Success(filteredNotifications)
                        _unreadCount.value = filteredNotifications.count { !it.isRead }
                        isInitialLoad = false
                    }
                    .onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Failed to load notifications")
                        isInitialLoad = false
                    }
            }
        }
    }

    /**
     * Observe notifications flow for real-time updates
     * This will automatically update UI when new notifications arrive
     */
    private fun observeNotificationsFlow() {
        getNotificationsFlowUseCase()
            .onEach { notifications ->
                // Filter notifications by current user
                val filteredNotifications = filterNotificationsByCurrentUser(notifications)

                // Update UI state without showing loading
                val currentState = _uiState.value
                if (currentState !is UiState.Loading || !isInitialLoad) {
                    _uiState.value = UiState.Success(filteredNotifications)
                    _unreadCount.value = filteredNotifications.count { !it.isRead }
                }
            }
            .catch { error ->
                // Only show error if not already in error state or if initial load failed
                if (isInitialLoad) {
                    _uiState.value = UiState.Error(error.message ?: "Failed to load notifications")
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Filter notifications to only show those meant for current logged-in user
     * This prevents showing notifications from other users on the same device
     */
    private fun filterNotificationsByCurrentUser(notifications: List<Notification>): List<Notification> {
        val currentUserId = tokenManager.getUserId()

        if (currentUserId.isNullOrBlank()) {
            return notifications
        }

        val normalizedCurrentUserId = currentUserId.trim()

        return notifications.filter { notification ->
            val targetUserId = notification.actionData?.extraData?.resolveTargetUserId()

            if (!targetUserId.isNullOrBlank()) {
                targetUserId.equals(normalizedCurrentUserId, ignoreCase = true)
            } else {
                when (notification.type) {
                    Notification.NotificationType.MATCH -> {
                        val matchedUserId = notification.actionData?.userId
                        matchedUserId.isNullOrBlank() ||
                            matchedUserId.trim().equals(normalizedCurrentUserId, ignoreCase = true)
                    }
                    else -> true
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            markNotificationAsReadUseCase(notificationId)
            // No need to reload - Flow will automatically update
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            markAllNotificationsAsReadUseCase()
            // No need to reload - Flow will automatically update
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            deleteNotificationUseCase(notificationId)
            // No need to reload - Flow will automatically update
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            deleteAllNotificationsUseCase()
            // No need to reload - Flow will automatically update
        }
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            // Refresh by loading once and then continue observing flow
            getNotificationsUseCase()
                .onSuccess { notifications ->
                    // Filter notifications by current user
                    val filteredNotifications = filterNotificationsByCurrentUser(notifications)
                    _uiState.value = UiState.Success(filteredNotifications)
                    _unreadCount.value = filteredNotifications.count { !it.isRead }
                }
        }
    }
}
