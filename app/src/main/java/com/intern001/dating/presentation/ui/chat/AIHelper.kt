package com.intern001.dating.presentation.ui.chat

import com.intern001.dating.domain.model.MatchCard

object AIHelper {
    fun isAIUser(userId: String?): Boolean {
        return userId == AIConstants.AI_ASSISTANT_USER_ID
    }

    fun getAIDisplayName(profile: MatchCard?): String {
        return profile?.displayName
            ?: "${profile?.firstName ?: ""} ${profile?.lastName ?: ""}".trim()
                .takeIf { it.isNotEmpty() }
            ?: AIConstants.AI_ASSISTANT_NAME
    }

    fun getAIAvatarUrl(profile: MatchCard?): String? {
        return profile?.avatar
            ?: profile?.photos?.firstOrNull()?.url
            ?: AIConstants.AI_FAKE_AVATAR_URL
    }

    fun formatAIProfileInfo(profile: MatchCard): String {
        val parts = mutableListOf<String>()

        profile.age?.let { parts.add("$it tuổi") }
        profile.location?.city?.let { parts.add(it) }
        profile.occupation?.let { parts.add(it) }

        return parts.joinToString(" • ")
    }

    fun getAIBio(profile: MatchCard?): String {
        return profile?.bio ?: AIConstants.AI_FAKE_BIO
    }

    fun getAIInterests(profile: MatchCard?): List<String> {
        return profile?.interests?.takeIf { it.isNotEmpty() }
            ?: AIConstants.AI_FAKE_INTERESTS
    }
}

