package com.intern001.dating.data.model.response

import com.intern001.dating.domain.model.Match
import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.domain.model.MatchCriteria
import com.intern001.dating.domain.model.MatchResult
import com.intern001.dating.domain.model.MatchStatus
import com.intern001.dating.domain.model.Photo
import com.intern001.dating.domain.model.Range
import com.intern001.dating.domain.model.UpdateProfile
import com.intern001.dating.domain.model.UserLocation
import com.intern001.dating.domain.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val dateFormat =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

fun MatchCardResponse.toMatchCard(): MatchCard {
    return MatchCard(
        id = id,
        userId = userId,
        firstName = firstName,
        lastName = lastName,
        displayName = displayName ?: "${firstName ?: ""} ${lastName ?: ""}".trim().takeIf { it.isNotEmpty() } ?: "Unknown",
        age = age,
        gender = gender,
        avatar = avatar,
        photos =
        photos?.mapNotNull { photoResponse ->
            // Get ID from either _id or id field (backend may use either)
            val photoId = photoResponse.id ?: photoResponse.idAlt ?: "photo_${System.currentTimeMillis()}_${photoResponse.order ?: 0}"
            Photo(
                id = photoId,
                userId = photoResponse.userId,
                url = photoResponse.url,
                cloudinaryPublicId = photoResponse.cloudinaryPublicId,
                type = photoResponse.type ?: "gallery",
                source = photoResponse.source ?: "upload",
                isPrimary = photoResponse.isPrimary ?: false,
                order = photoResponse.order ?: 0,
                isVerified = photoResponse.isVerified ?: false,
                width = photoResponse.width,
                height = photoResponse.height,
                fileSize = photoResponse.fileSize,
                format = photoResponse.format,
                isActive = photoResponse.isActive ?: true,
                createdAt =
                photoResponse.createdAt?.let {
                    try {
                        dateFormat.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                },
                updatedAt =
                photoResponse.updatedAt?.let {
                    try {
                        dateFormat.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            )
        }
            ?: emptyList(),
        bio = bio,
        distance = distance,
        location =
        location?.let {
            UserLocation(
                latitude = it.latitude,
                longitude = it.longitude,
                city = it.city,
                country = it.country,
            )
        },
        occupation = occupation,
        company = company,
        education = education,
        interests = interests ?: emptyList(),
        relationshipMode = relationshipMode,
        height = height,
        zodiacSign = zodiacSign,
        isVerified = isVerified ?: false,
    )
}

fun UserProfileResponse.toUserProfile(): UserProfile {
    // Get ID from either _id or id field (backend may use either)
    val profileId = id ?: idAlt ?: userId

    return UserProfile(
        id = profileId,
        userId = userId,
        firstName = firstName,
        lastName = lastName,
        displayName = displayName ?: "${firstName ?: ""} ${lastName ?: ""}".trim().takeIf { it.isNotEmpty() } ?: "Unknown",
        dateOfBirth = dateOfBirth?.let {
            try {
                dateFormat.parse(it)
            } catch (e: Exception) {
                null
            }
        },
        avatar = avatar,
        bio = bio,
        age = age,
        gender = gender,
        interests = interests ?: emptyList(),
        mode = mode,
        relationshipMode = relationshipMode,
        height = height,
        weight = weight,
        location =
        location?.let {
            UserLocation(
                latitude = it.latitude,
                longitude = it.longitude,
                city = it.city,
                country = it.country,
            )
        },
        city = city,
        country = country,
        occupation = occupation,
        company = company,
        education = education,
        zodiacSign = zodiacSign,
        verifiedAt = verifiedAt?.let {
            try {
                dateFormat.parse(it)
            } catch (e: Exception) {
                null
            }
        },
        verifiedBadge = verifiedBadge ?: false,
        isVerified = isVerified ?: false,
        photos =
        photos?.mapNotNull {
            // Get ID from either _id or id field
            val photoId = it.id ?: it.idAlt ?: "photo_${System.currentTimeMillis()}_${it.order ?: 0}"
            Photo(
                id = photoId,
                userId = it.userId,
                url = it.url,
                cloudinaryPublicId = it.cloudinaryPublicId,
                type = it.type ?: "gallery",
                source = it.source ?: "upload",
                isPrimary = it.isPrimary ?: false,
                order = it.order ?: 0,
                isVerified = it.isVerified ?: false,
                width = it.width,
                height = it.height,
                fileSize = it.fileSize,
                format = it.format,
                isActive = it.isActive ?: true,
                createdAt =
                it.createdAt?.let { dateStr ->
                    try {
                        dateFormat.parse(dateStr)
                    } catch (e: Exception) {
                        null
                    }
                },
                updatedAt =
                it.updatedAt?.let { dateStr ->
                    try {
                        dateFormat.parse(dateStr)
                    } catch (e: Exception) {
                        null
                    }
                },
            )
        }
            ?: emptyList(),
        profileCompleteness = profileCompleteness ?: 0,
        profileViews = profileViews ?: 0,
        goals = goals,
        job = job,
        openQuestionAnswers = openQuestionAnswers,
        createdAt =
        try {
            dateFormat.parse(createdAt) ?: Date()
        } catch (e: Exception) {
            Date()
        },
        updatedAt =
        try {
            dateFormat.parse(updatedAt) ?: Date()
        } catch (e: Exception) {
            Date()
        },
    )
}

fun MatchResponse.toMatch(): Match {
    return Match(
        id = id,
        userId = userId,
        matchedUserId = matchedUserId,
        matchedUser = matchedUser.toUserProfile(),
        status =
        when (status.lowercase()) {
            "pending" -> MatchStatus.PENDING
            "matched" -> MatchStatus.MATCHED
            "expired" -> MatchStatus.EXPIRED
            "unmatched" -> MatchStatus.UNMATCHED
            else -> MatchStatus.PENDING
        },
        createdAt =
        try {
            dateFormat.parse(createdAt) ?: Date()
        } catch (e: Exception) {
            Date()
        },
        matchedAt =
        matchedAt?.let {
            try {
                dateFormat.parse(it)
            } catch (e: Exception) {
                null
            }
        },
    )
}

fun MatchResultResponse.toMatchResult(): MatchResult {
    return MatchResult(
        isMatch = isMatch,
        matchId = matchId,
        matchedUser = matchedUser?.toUserProfile(),
    )
}

fun RecommendationCriteriaResponse.toMatchCriteria(): MatchCriteria {
    return MatchCriteria(
        seekingGender = seekingGender ?: emptyList(),
        ageRange = ageRange?.let { Range(min = it.min, max = it.max) },
        distanceRange = distanceRange?.let { Range(min = it.min, max = it.max) },
        interests = interests ?: emptyList(),
        relationshipModes = relationshipModes ?: emptyList(),
        heightRange = heightRange?.let { Range(min = it.min, max = it.max) },
    )
}

fun UserProfile.toUpdateProfile(): UpdateProfile {
    return UpdateProfile(
        id = id,
        userId = userId,
        firstName = firstName,
        lastName = lastName,
        displayName = displayName,
        avatar = avatar,
        bio = bio,
        age = age,
        gender = gender,
        interests = interests,
        relationshipMode = relationshipMode,
        height = height,
        weight = weight,
        location = location,
        occupation = occupation,
        company = company,
        education = education,
        zodiacSign = zodiacSign,
        photos = photos.map { it.url },
        profileCompleteness = profileCompleteness,
        profileViews = profileViews,
        mode = mode,
        verifiedAt = verifiedAt,
        selfieImage = selfieImage,
        verifiedBadge = verifiedBadge,
        job = job,
        goals = goals,
        isVerified = isVerified,
        openQuestionAnswers = openQuestionAnswers,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
