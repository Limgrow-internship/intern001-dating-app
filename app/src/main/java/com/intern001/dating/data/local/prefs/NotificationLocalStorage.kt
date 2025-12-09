package com.intern001.dating.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intern001.dating.domain.model.Notification
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class NotificationLocalStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
) {
    companion object {
        private const val PREFS_NAME = "notifications_prefs"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_NEXT_ID = "next_notification_id"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Flow to observe notification changes
    val notificationsFlow: Flow<List<Notification>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_NOTIFICATIONS) {
                trySend(getAllNotifications())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        // Emit initial value
        trySend(getAllNotifications())

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    fun saveNotification(notification: Notification) {
        val notifications = getAllNotifications().toMutableList()
        notifications.add(0, notification) // Add to top
        saveAllNotifications(notifications)
    }

    fun getAllNotifications(): List<Notification> {
        val json = prefs.getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Notification>>() {}.type
            val parsed: List<Notification>? = gson.fromJson(json, type)
            (parsed ?: emptyList()).sortedByDescending { it.timestamp.time }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAllNotifications(notifications: List<Notification>) {
        val json = gson.toJson(notifications)
        prefs.edit().putString(KEY_NOTIFICATIONS, json).apply()
    }

    fun markAsRead(notificationId: String) {
        val updated = getAllNotifications().map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        saveAllNotifications(updated)
    }

    fun markAllAsRead() {
        val updated = getAllNotifications().map { it.copy(isRead = true) }
        saveAllNotifications(updated)
    }

    fun deleteNotification(notificationId: String) {
        val updated = getAllNotifications().filterNot { it.id == notificationId }
        saveAllNotifications(updated)
    }

    fun deleteAllNotifications() {
        prefs.edit().remove(KEY_NOTIFICATIONS).apply()
    }

    fun getUnreadCount(): Int {
        return getAllNotifications().count { !it.isRead }
    }

    fun getNextNotificationId(): String {
        val nextId = prefs.getInt(KEY_NEXT_ID, 1)
        prefs.edit().putInt(KEY_NEXT_ID, nextId + 1).apply()
        return nextId.toString()
    }
}
