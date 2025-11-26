package com.intern001.dating.domain.model

import java.util.Date

data class MatchCard(
    val id: String,
    val userId: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val displayName: String,
    val age: Int?,
    val gender: String?,
    val avatar: String?,
    val photos: List<Photo> = emptyList(),
    val bio: String?,
    val distance: Double?,
    val location: UserLocation?,
    val occupation: String?,
    val company: String?,
    val education: String?,
    val interests: List<String> = emptyList(),
    val relationshipMode: String?,
    val height: Int?,
    val zodiacSign: String?,
    val isVerified: Boolean = false,
)

data class Match(
    val id: String,
    val userId: String,
    val matchedUserId: String,
    val matchedUser: UserProfile,
    val status: MatchStatus,
    val createdAt: Date,
    val matchedAt: Date?,
)

enum class MatchStatus {
    PENDING,
    MATCHED,
    EXPIRED,
    UNMATCHED,
}

data class MatchAction(
    val userId: String,
    val targetUserId: String,
    val actionType: MatchActionType,
    val timestamp: Date = Date(),
)

enum class MatchActionType {
    LIKE,
    PASS,
    SUPER_LIKE,
    BLOCK,
}

data class MatchCriteria(
    val seekingGender: List<String> = emptyList(),
    val ageRange: Range? = null,
    val distanceRange: Range? = null,
    val interests: List<String> = emptyList(),
    val relationshipModes: List<String> = emptyList(),
    val heightRange: Range? = null,
)

data class MatchResult(
    val isMatch: Boolean,
    val matchId: String?,
    val matchedUser: UserProfile?,
)

data class MatchList(
    val conversationId: String,
    val matchId: String,
    val lastActivityAt: String,
    val matchedUser: UserProfileMatch,
)

data class UserProfileMatch(
    val userId: String,
    val name: String,
    val avatarUrl: String?,
    val age: Int?,
    val city: String?,
)
