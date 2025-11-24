package com.intern001.dating.data.model.response

import com.google.gson.annotations.SerializedName
import com.intern001.dating.domain.model.Notification
import java.util.Date

/**
 * DTO for Notification response from API
 */
data class NotificationResponseDTO(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String, // "like", "superlike", "match", "verification_success", etc.
    @SerializedName("title")
    val title: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("timestamp")
    val timestamp: Long, // Unix timestamp in milliseconds
    @SerializedName("isRead")
    val isRead: Boolean = false,
    @SerializedName("actionData")
    val actionData: NotificationActionDataDTO? = null,
) {
    fun toDomain(): Notification {
        val notificationType = when (type.lowercase()) {
            "like" -> Notification.NotificationType.LIKE
            "superlike" -> Notification.NotificationType.SUPERLIKE
            "match" -> Notification.NotificationType.MATCH
            "verification_success" -> Notification.NotificationType.VERIFICATION_SUCCESS
            "verification_failed" -> Notification.NotificationType.VERIFICATION_FAILED
            "premium_upgrade" -> Notification.NotificationType.PREMIUM_UPGRADE
            else -> Notification.NotificationType.OTHER
        }

        val iconType = when (notificationType) {
            Notification.NotificationType.LIKE,
            Notification.NotificationType.SUPERLIKE -> Notification.NotificationIconType.HEART
            Notification.NotificationType.MATCH -> Notification.NotificationIconType.MATCH
            else -> Notification.NotificationIconType.SETTINGS
        }

        return Notification(
            id = id,
            type = notificationType,
            title = title,
            message = message,
            timestamp = Date(timestamp),
            isRead = isRead,
            iconType = iconType,
            actionData = actionData?.toDomain(),
        )
    }
}

data class NotificationActionDataDTO(
    @SerializedName("navigateTo")
    val navigateTo: String? = null,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("matchId")
    val matchId: String? = null,
    @SerializedName("likerId")
    val likerId: String? = null,
    @SerializedName("extraData")
    val extraData: Map<String, String>? = null,
) {
    fun toDomain(): Notification.NotificationActionData {
        return Notification.NotificationActionData(
            navigateTo = navigateTo,
            userId = userId,
            matchId = matchId,
            likerId = likerId,
            extraData = extraData ?: emptyMap()
        )
    }
}

data class NotificationsListResponse(
    @SerializedName("notifications")
    val notifications: List<NotificationResponseDTO>,
    @SerializedName("total")
    val total: Int? = null,
    @SerializedName("unreadCount")
    val unreadCount: Int? = null,
)

