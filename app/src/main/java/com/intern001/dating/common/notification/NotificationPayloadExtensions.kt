package com.intern001.dating.common.notification

/**
 * Helper to normalize different backend payload keys that indicate
 * which user a notification is targeting.
 */
fun Map<String, String>.resolveTargetUserId(): String? {
    val candidateKeys =
        listOf(
            "targetUserId",
            "target_user_id",
            "userId",
            "user_id",
            "receiverId",
        )

    return candidateKeys
        .asSequence()
        .mapNotNull { key -> this[key] }
        .firstOrNull { !it.isNullOrBlank() }
        ?.trim()
}

