package com.intern001.dating.domain.repository

import com.intern001.dating.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun getNotifications(): Result<List<Notification>>
    fun getNotificationsFlow(): Flow<List<Notification>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    suspend fun deleteAllNotifications(): Result<Unit>
    suspend fun saveNotification(notification: Notification): Result<Unit>
    fun getUnreadCount(): Int
}
