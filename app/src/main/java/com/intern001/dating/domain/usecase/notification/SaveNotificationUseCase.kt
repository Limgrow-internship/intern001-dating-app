package com.intern001.dating.domain.usecase.notification

import com.intern001.dating.domain.model.Notification
import com.intern001.dating.domain.repository.NotificationRepository
import javax.inject.Inject

class SaveNotificationUseCase
@Inject
constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(notification: Notification): Result<Unit> {
        if (notification.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Notification ID cannot be blank"))
        }
        if (notification.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Notification title cannot be blank"))
        }
        return notificationRepository.saveNotification(notification)
    }
}
