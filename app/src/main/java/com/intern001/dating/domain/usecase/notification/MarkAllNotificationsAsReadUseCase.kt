package com.intern001.dating.domain.usecase.notification

import com.intern001.dating.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkAllNotificationsAsReadUseCase
@Inject
constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return notificationRepository.markAllAsRead()
    }
}

