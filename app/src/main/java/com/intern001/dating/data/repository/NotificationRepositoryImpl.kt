package com.intern001.dating.data.repository

import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.prefs.NotificationLocalStorage
import com.intern001.dating.domain.model.Notification
import com.intern001.dating.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    @Named("datingApi") private val apiService: DatingApiService,
    private val localStorage: NotificationLocalStorage,
) : NotificationRepository {

    override suspend fun getNotifications(): Result<List<Notification>> = withContext(Dispatchers.IO) {
        try {
            // TODO: Fetch from API when endpoint is available
            // val response = apiService.getNotifications()
            // if (response.isSuccessful) {
            //     val notifications = response.body()?.notifications?.map { it.toDomain() } ?: emptyList()
            //     // Save to local storage
            //     notifications.forEach { localStorage.saveNotification(it) }
            //     Result.success(notifications)
            // } else {
            //     // Fallback to local storage
            //     Result.success(localStorage.getAllNotifications())
            // }

            // For now, return from local storage
            Result.success(localStorage.getAllNotifications())
        } catch (e: Exception) {
            // Fallback to local storage on error
            Result.success(localStorage.getAllNotifications())
        }
    }

    override fun getNotificationsFlow(): Flow<List<Notification>> {
        return localStorage.notificationsFlow
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            localStorage.markAsRead(notificationId)
            // TODO: Sync with API when endpoint is available
            // apiService.markNotificationAsRead(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            localStorage.markAllAsRead()
            // TODO: Sync with API when endpoint is available
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            localStorage.deleteNotification(notificationId)
            // TODO: Sync with API when endpoint is available
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllNotifications(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            localStorage.deleteAllNotifications()
            // TODO: Sync with API when endpoint is available
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveNotification(notification: Notification): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            localStorage.saveNotification(notification)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUnreadCount(): Int {
        return localStorage.getUnreadCount()
    }
}
