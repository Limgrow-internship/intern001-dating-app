package com.intern001.dating.domain.model

import java.util.Date

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: Date,
    val isRead: Boolean = false,
    val iconType: NotificationIconType = NotificationIconType.SETTINGS,
    val actionData: NotificationActionData? = null,
) {
    enum class NotificationType {
        LIKE,
        SUPERLIKE,
        MATCH,
        VERIFICATION_SUCCESS,
        VERIFICATION_FAILED,
        PREMIUM_UPGRADE,
        OTHER,
    }

    enum class NotificationIconType {
        HEART,
        MATCH,
        SETTINGS,
    }

    data class NotificationActionData(
        val navigateTo: String? = null,
        val userId: String? = null,
        val matchId: String? = null,
        val likerId: String? = null,
        val extraData: Map<String, String> = emptyMap(),
    )
}
