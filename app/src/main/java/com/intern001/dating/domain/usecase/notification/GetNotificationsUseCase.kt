package com.intern001.dating.domain.usecase.notification

import com.intern001.dating.domain.model.Notification
import com.intern001.dating.domain.repository.NotificationRepository
import javax.inject.Inject

class GetNotificationsUseCase
@Inject
constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(): Result<List<Notification>> {
        return notificationRepository.getNotifications()
    }
}
