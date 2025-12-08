package com.intern001.dating.presentation.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.intern001.dating.R
import com.intern001.dating.data.local.NotificationSettingsStorage
import com.intern001.dating.databinding.FragmentNotificationSettingBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationSettingFragment : BaseFragment() {

    private var _binding: FragmentNotificationSettingBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var notificationSettingsStorage: NotificationSettingsStorage

    private val toggleDefaults = mapOf(
        R.id.toggleShowNotifications to true,
        R.id.toggleNewMatch to true,
        R.id.toggleNewMessage to true,
        R.id.toggleLikes to false,
        R.id.toggleDiscoverySuggestedProfiles to false,
        R.id.toggleDiscoveryNearbyUpdates to false,
        R.id.toggleAppInfoSuggestedProfiles to false,
        R.id.toggleAppInfoNearbyUpdates to false,
    )

    private val toggleKeys = mapOf(
        R.id.toggleShowNotifications to NotificationSettingsStorage.KEY_SHOW_NOTIFICATIONS,
        R.id.toggleNewMatch to NotificationSettingsStorage.KEY_NEW_MATCH,
        R.id.toggleNewMessage to NotificationSettingsStorage.KEY_NEW_MESSAGE,
        R.id.toggleLikes to NotificationSettingsStorage.KEY_LIKES,
        R.id.toggleDiscoverySuggestedProfiles to NotificationSettingsStorage.KEY_DISCOVERY_SUGGESTED,
        R.id.toggleDiscoveryNearbyUpdates to NotificationSettingsStorage.KEY_DISCOVERY_NEARBY,
        R.id.toggleAppInfoSuggestedProfiles to NotificationSettingsStorage.KEY_APP_INFO_SUGGESTED,
        R.id.toggleAppInfoNearbyUpdates to NotificationSettingsStorage.KEY_APP_INFO_NEARBY,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNotificationSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupToggles()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupToggles() {
        toggleDefaults.forEach { (viewId, defaultState) ->
            val toggleView = binding.root.findViewById<ImageView>(viewId) ?: return@forEach
            val key = toggleKeys[viewId] ?: return@forEach

            val storedState = notificationSettingsStorage.getSetting(key)
            val appliedState = storedState ?: defaultState
            applyToggleIcon(toggleView, appliedState)

            toggleView.setOnClickListener {
                val currentState = toggleView.getTag(viewId) as? Boolean ?: appliedState
                val newState = !currentState
                applyToggleIcon(toggleView, newState)
                notificationSettingsStorage.setSetting(key, newState)
            }
        }
    }

    private fun applyToggleIcon(view: ImageView, isEnabled: Boolean) {
        view.setImageResource(
            if (isEnabled) R.drawable.ic_toggle_active else R.drawable.ic_toggle_unactive,
        )
        view.setTag(view.id, isEnabled)
    }
}
