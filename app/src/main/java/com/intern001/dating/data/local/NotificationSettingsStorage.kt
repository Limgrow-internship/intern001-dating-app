package com.intern001.dating.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettingsStorage @Inject constructor(
    @ApplicationContext context: Context,
) {
    companion object {
        private const val PREFS_NAME = "notification_settings_prefs"
        const val KEY_SHOW_NOTIFICATIONS = "show_notifications"
        const val KEY_NEW_MATCH = "new_match"
        const val KEY_NEW_MESSAGE = "new_message"
        const val KEY_LIKES = "likes"
        const val KEY_DISCOVERY_SUGGESTED = "discovery_suggested_profiles"
        const val KEY_DISCOVERY_NEARBY = "discovery_nearby_updates"
        const val KEY_APP_INFO_SUGGESTED = "app_info_suggested_profiles"
        const val KEY_APP_INFO_NEARBY = "app_info_nearby_updates"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSetting(key: String): Boolean? = if (prefs.contains(key)) prefs.getBoolean(key, false) else null

    fun setSetting(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}
