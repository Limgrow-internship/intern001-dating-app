package com.intern001.dating.domain.usecase.notification

import com.intern001.dating.domain.model.Notification
import com.intern001.dating.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsFlowUseCase
@Inject
constructor(
    private val notificationRepository: NotificationRepository,
) {
    operator fun invoke(): Flow<List<Notification>> {
        return notificationRepository.getNotificationsFlow()
    }
}

