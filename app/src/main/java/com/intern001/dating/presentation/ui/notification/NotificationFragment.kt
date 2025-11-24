package com.intern001.dating.presentation.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentNotificationBinding
import com.intern001.dating.domain.model.Notification
import com.intern001.dating.presentation.common.state.UiState
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.navigation.navigateToChatDetail
import com.intern001.dating.presentation.navigation.navigateToProfileDetail
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationFragment : BaseFragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationViewModel by viewModels()

    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show bottom navigation when in NotificationFragment
        (activity as? MainActivity)?.hideBottomNavigation(false)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        // Notifications will auto-update via Flow
        // Reset flag when user returns to screen to mark as read again if needed
        hasMarkedAllAsReadOnView = false
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            handleNotificationClick(notification)
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }

    private fun setupListeners() {
        binding.btnRetry.setOnClickListener {
            viewModel.refreshNotifications()
        }

        binding.ivFilter.setOnClickListener {
            // TODO: Implement filter/settings functionality
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        showLoading()
                    }
                    is UiState.Success -> {
                        hideLoading()
                        if (state.data.isEmpty()) {
                            showEmptyState()
                        } else {
                            showNotifications(state.data)
                        }
                    }
                    is UiState.Error -> {
                        hideLoading()
                        showError(state.message ?: "Failed to load notifications")
                    }
                    is UiState.Idle -> {
                        hideLoading()
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.emptyStateLayout.isVisible = false
        binding.rvNotifications.isVisible = false
        binding.errorLayout.isVisible = false
    }

    private fun hideLoading() {
        binding.progressBar.isVisible = false
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.isVisible = true
        binding.rvNotifications.isVisible = false
        binding.errorLayout.isVisible = false
    }

    private var hasMarkedAllAsReadOnView = false

    private fun showNotifications(notifications: List<Notification>) {
        binding.emptyStateLayout.isVisible = false
        binding.rvNotifications.isVisible = true
        binding.errorLayout.isVisible = false
        
        // Update adapter with new list (DiffUtil will handle updates efficiently)
        notificationAdapter.submitList(notifications) {
            // Mark all as read after list is displayed (only once when first viewing)
            if (!hasMarkedAllAsReadOnView && notifications.isNotEmpty()) {
                hasMarkedAllAsReadOnView = true
                viewModel.markAllAsRead()
            }
        }
    }

    private fun showError(message: String) {
        binding.emptyStateLayout.isVisible = false
        binding.rvNotifications.isVisible = false
        binding.errorLayout.isVisible = true
        binding.tvError.text = message
    }

    private fun handleNotificationClick(notification: Notification) {
        // Mark as read
        if (!notification.isRead) {
            viewModel.markAsRead(notification.id)
        }

        // Handle navigation based on notification type and action data
        val actionData = notification.actionData
        when (notification.type) {
            Notification.NotificationType.MATCH -> {
                actionData?.userId?.let { userId ->
                    navController.navigateToChatDetail(userId)
                }
                // If no userId, just mark as read (already done above)
            }
            Notification.NotificationType.LIKE,
            Notification.NotificationType.SUPERLIKE -> {
                // Navigate to premium or profile
                actionData?.navigateTo?.let { navigateTo ->
                    when (navigateTo) {
                        "premium" -> {
                            // TODO: Navigate to premium screen
                        }
                        "profile" -> {
                            actionData.likerId?.let { userId ->
                                navController.navigateToProfileDetail(userId)
                            }
                        }
                    }
                }
            }
            Notification.NotificationType.VERIFICATION_SUCCESS,
            Notification.NotificationType.VERIFICATION_FAILED -> {
                // TODO: Navigate to verification screen if needed
            }
            Notification.NotificationType.PREMIUM_UPGRADE -> {
                // TODO: Navigate to premium screen
            }
            Notification.NotificationType.OTHER -> {
                // Handle other notification types
            }
        }
    }
}
