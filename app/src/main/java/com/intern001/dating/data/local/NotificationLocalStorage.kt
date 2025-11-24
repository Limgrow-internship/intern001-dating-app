package com.intern001.dating.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intern001.dating.domain.model.Notification
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

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

    /**
     * Save a notification to local storage
     */
    fun saveNotification(notification: Notification) {
        val notifications = getAllNotifications().toMutableList()
        notifications.add(0, notification) // Add to top
        saveAllNotifications(notifications)
    }

    /**
     * Get all notifications, sorted by timestamp (newest first)
     */
    fun getAllNotifications(): List<Notification> {
        val json = prefs.getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<NotificationStorageDTO>>() {}.type
        return try {
            val dtos: List<NotificationStorageDTO> = gson.fromJson(json, type) ?: return emptyList()
            dtos.map { it.toDomain() }
                .sortedByDescending { it.timestamp } // Sort by newest first
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get unread notifications count
     */
    fun getUnreadCount(): Int {
        return getAllNotifications().count { !it.isRead }
    }

    /**
     * Mark notification as read
     */
    fun markAsRead(notificationId: String) {
        val notifications = getAllNotifications().toMutableList()
        val index = notifications.indexOfFirst { it.id == notificationId }
        if (index >= 0) {
            notifications[index] = notifications[index].copy(isRead = true)
            saveAllNotifications(notifications)
        }
    }

    /**
     * Mark all notifications as read
     */
    fun markAllAsRead() {
        val notifications = getAllNotifications().map { it.copy(isRead = true) }
        saveAllNotifications(notifications)
    }

    /**
     * Delete a notification
     */
    fun deleteNotification(notificationId: String) {
        val notifications = getAllNotifications().toMutableList()
        notifications.removeAll { it.id == notificationId }
        saveAllNotifications(notifications)
    }

    /**
     * Delete all notifications
     */
    fun deleteAllNotifications() {
        prefs.edit().remove(KEY_NOTIFICATIONS).apply()
    }

    /**
     * Get next notification ID
     */
    fun getNextNotificationId(): String {
        val nextId = prefs.getLong(KEY_NEXT_ID, 1)
        prefs.edit().putLong(KEY_NEXT_ID, nextId + 1).apply()
        return "notif_$nextId"
    }

    private fun saveAllNotifications(notifications: List<Notification>) {
        val dtos = notifications.map { NotificationStorageDTO.fromDomain(it) }
        val json = gson.toJson(dtos)
        prefs.edit().putString(KEY_NOTIFICATIONS, json).apply()
        // Flow will automatically emit new value via SharedPreferences listener
    }
}

