package com.intern001.dating.presentation.ui.notification

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentNotificationBinding
import com.intern001.dating.domain.model.Notification
import com.intern001.dating.presentation.common.state.UiState
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.navigation.navigateToChatDetail
import com.intern001.dating.presentation.navigation.navigateToDatingMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationFragment : BaseFragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationViewModel by viewModels()

    private lateinit var notificationAdapter: NotificationAdapter

    private var cachedNotifications: List<Notification> = emptyList()
    private var currentFilter: NotificationFilter = NotificationFilter.ALL

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

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            handleNotificationClick(notification)
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }

        attachSwipeToDelete()
    }

    private fun setupListeners() {
        binding.btnRetry.setOnClickListener {
            viewModel.refreshNotifications()
        }

        binding.ivFilter.setOnClickListener {
            showFilterDialog()
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
                        showNotifications(state.data)
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

    private fun showNotifications(notifications: List<Notification>) {
        cachedNotifications = notifications
        applyFilterAndShow()
    }

    private fun applyFilterAndShow() {
        val filtered = when (currentFilter) {
            NotificationFilter.ALL -> cachedNotifications
            NotificationFilter.UNREAD -> cachedNotifications.filter { !it.isRead }
            NotificationFilter.LIKE -> cachedNotifications.filter {
                it.type == Notification.NotificationType.LIKE || it.type == Notification.NotificationType.SUPERLIKE
            }
            NotificationFilter.MATCH -> cachedNotifications.filter { it.type == Notification.NotificationType.MATCH }
            NotificationFilter.OTHER -> cachedNotifications.filter {
                it.type != Notification.NotificationType.LIKE &&
                    it.type != Notification.NotificationType.SUPERLIKE &&
                    it.type != Notification.NotificationType.MATCH
            }
        }
        renderFilteredNotifications(filtered)
    }

    private fun renderFilteredNotifications(filtered: List<Notification>) {
        if (filtered.isEmpty()) {
            showEmptyState()
            notificationAdapter.submitList(emptyList())
            return
        }

        binding.emptyStateLayout.isVisible = false
        binding.rvNotifications.isVisible = true
        binding.errorLayout.isVisible = false

        notificationAdapter.submitList(filtered)
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
                val matchId = actionData?.matchId
                val userId = actionData?.userId
                val matchedName = actionData?.extraData?.get("matchedUserName") ?: notification.title
                val matchedAvatar = actionData?.extraData?.get("matchedUserAvatar") ?: ""

                if (matchId != null) {
                    val args = bundleOf(
                        "matchId" to matchId,
                        "matchedUserName" to matchedName,
                        "matchedUserAvatar" to matchedAvatar,
                    )
                    navController.navigate(R.id.action_notification_to_chatDetail, args)
                } else if (userId != null) {
                    navController.navigateToChatDetail(userId)
                }
            }
            Notification.NotificationType.LIKE,
            Notification.NotificationType.SUPERLIKE,
            -> {
                val targetId = actionData?.likerId ?: actionData?.userId
                if (targetId.isNullOrBlank()) {
                    Toast.makeText(
                        context,
                        getString(R.string.not_found_user_liked),
                        Toast.LENGTH_SHORT,
                    ).show()
                    return
                }
                navController.navigateToDatingMode(
                    likerId = targetId,
                    targetUserId = targetId,
                    allowMatchedProfile = true,
                )
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

    private fun showFilterDialog() {
        val options = arrayOf(
            getString(R.string.all),
            getString(R.string.havent_read),
            getString(R.string.likes),
            getString(R.string.match),
            getString(R.string.other),
        )
        val currentIndex = currentFilter.ordinal

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.filter_notification))
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                currentFilter = NotificationFilter.values()[which]
                dialog.dismiss()
                applyFilterAndShow()
            }
            .setNegativeButton("Đóng", null)
            .show()
    }

    private fun attachSwipeToDelete() {
        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
        val backgroundPaint = Paint().apply {
            color = ContextCompat.getColor(requireContext(), R.color.notification_delete_bg)
            isAntiAlias = true
        }
        val buttonWidthPx = (72 * resources.displayMetrics.density).toInt()

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val notification = notificationAdapter.currentList.getOrNull(position)
                if (notification != null) {
                    cachedNotifications = cachedNotifications.filter { it.id != notification.id }
                    viewModel.deleteNotification(notification.id)
                    applyFilterAndShow()
                } else {
                    notificationAdapter.notifyItemChanged(position)
                }
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 0.2f
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean,
            ) {
                val itemView = viewHolder.itemView
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    val clampedDx = dX.coerceAtLeast(-buttonWidthPx.toFloat())
                    val translationX = clampedDx

                    val backgroundRight = itemView.right.toFloat()
                    val backgroundLeft = backgroundRight + translationX
                    val background = RectF(
                        backgroundLeft,
                        itemView.top.toFloat(),
                        backgroundRight,
                        itemView.bottom.toFloat(),
                    )
                    c.drawRect(background, backgroundPaint)

                    deleteIcon?.let { icon ->
                        val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
                        val iconTop = itemView.top + iconMargin
                        val iconBottom = iconTop + icon.intrinsicHeight
                        val iconRight = itemView.right - iconMargin
                        val iconLeft = iconRight - icon.intrinsicWidth
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        icon.draw(c)
                    }

                    itemView.translationX = translationX
                } else {
                    itemView.translationX = 0f
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvNotifications)
    }

    private enum class NotificationFilter {
        ALL,
        UNREAD,
        LIKE,
        MATCH,
        OTHER,
    }
}
