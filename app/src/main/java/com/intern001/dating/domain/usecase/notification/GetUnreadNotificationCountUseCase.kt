package com.intern001.dating.domain.usecase.notification

import com.intern001.dating.domain.repository.NotificationRepository
import javax.inject.Inject

class GetUnreadNotificationCountUseCase
@Inject
constructor(
    private val notificationRepository: NotificationRepository,
) {
    operator fun invoke(): Int {
        return notificationRepository.getUnreadCount()
    }
}
