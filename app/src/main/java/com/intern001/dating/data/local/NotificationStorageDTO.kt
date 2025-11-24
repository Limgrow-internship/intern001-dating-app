package com.intern001.dating.data.local

import com.intern001.dating.domain.model.Notification
import java.util.Date

/**
 * DTO for storing notifications in local storage
 * Uses Long timestamp instead of Date for Gson compatibility
 */
data class NotificationStorageDTO(
    val id: String,
    val type: String, // NotificationType enum name
    val title: String,
    val message: String,
    val timestamp: Long, // Unix timestamp in milliseconds
    val isRead: Boolean = false,
    val iconType: String, // NotificationIconType enum name
    val actionData: NotificationActionDataStorageDTO? = null,
) {
    fun toDomain(): Notification {
        return Notification(
            id = id,
            type = Notification.NotificationType.valueOf(type),
            title = title,
            message = message,
            timestamp = Date(timestamp),
            isRead = isRead,
            iconType = Notification.NotificationIconType.valueOf(iconType),
            actionData = actionData?.toDomain(),
        )
    }

    companion object {
        fun fromDomain(notification: Notification): NotificationStorageDTO {
            return NotificationStorageDTO(
                id = notification.id,
                type = notification.type.name,
                title = notification.title,
                message = notification.message,
                timestamp = notification.timestamp.time,
                isRead = notification.isRead,
                iconType = notification.iconType.name,
                actionData = notification.actionData?.let { NotificationActionDataStorageDTO.fromDomain(it) },
            )
        }
    }
}

data class NotificationActionDataStorageDTO(
    val navigateTo: String? = null,
    val userId: String? = null,
    val matchId: String? = null,
    val likerId: String? = null,
    val extraData: Map<String, String> = emptyMap(),
) {
    fun toDomain(): Notification.NotificationActionData {
        return Notification.NotificationActionData(
            navigateTo = navigateTo,
            userId = userId,
            matchId = matchId,
            likerId = likerId,
            extraData = extraData,
        )
    }

    companion object {
        fun fromDomain(actionData: Notification.NotificationActionData): NotificationActionDataStorageDTO {
            return NotificationActionDataStorageDTO(
                navigateTo = actionData.navigateTo,
                userId = actionData.userId,
                matchId = actionData.matchId,
                likerId = actionData.likerId,
                extraData = actionData.extraData,
            )
        }
    }
}
