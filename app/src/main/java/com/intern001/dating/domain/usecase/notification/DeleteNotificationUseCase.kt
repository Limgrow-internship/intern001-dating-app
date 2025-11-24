package com.intern001.dating.domain.usecase.notification

import com.intern001.dating.domain.repository.NotificationRepository
import javax.inject.Inject

class DeleteNotificationUseCase
@Inject
constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        if (notificationId.isBlank()) {
            return Result.failure(IllegalArgumentException("Notification ID cannot be blank"))
        }
        return notificationRepository.deleteNotification(notificationId)
    }
}

