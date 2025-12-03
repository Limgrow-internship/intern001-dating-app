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
import com.intern001.dating.databinding.FragmentNotificationBinding
import com.intern001.dating.domain.model.Notification
import com.intern001.dating.presentation.common.state.UiState
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.navigation.navigateToChatDetail
import com.intern001.dating.presentation.navigation.navigateToDatingMode
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

        notificationAdapter.submitList(notifications) {
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
        if (!notification.isRead) {
            viewModel.markAsRead(notification.id)
        }

        val actionData = notification.actionData
        when (notification.type) {
            Notification.NotificationType.MATCH -> {
                actionData?.userId?.let { userId ->
                    navController.navigateToChatDetail(userId)
                }
            }
            Notification.NotificationType.LIKE,
            Notification.NotificationType.SUPERLIKE,
            -> {
                val likerId = actionData?.likerId
                actionData?.navigateTo?.let { navigateTo ->
                    when (navigateTo) {
                        "dating_mode" -> {
                            navController.navigateToDatingMode(likerId)
                        }
                        "profile" -> {
                            likerId?.let { userId ->
                                navController.navigateToProfileDetail(userId)
                            }
                        }
                        "premium" -> {
                            // TODO: Navigate to premium screen
                        }
                        else -> {
                            likerId?.let {
                                navController.navigateToDatingMode(it)
                            }
                        }
                    }
                } ?: run {
                    likerId?.let {
                        navController.navigateToDatingMode(it)
                    }
                }
            }
            Notification.NotificationType.VERIFICATION_SUCCESS,
            Notification.NotificationType.VERIFICATION_FAILED,
            -> {
                // TODO: Navigate to verification screen if needed
            }
            Notification.NotificationType.PREMIUM_UPGRADE -> {
                // TODO: Navigate to premium screen
            }
            Notification.NotificationType.OTHER -> {
            }
        }
    }
}
