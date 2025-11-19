package com.intern001.dating.data.model.response

import com.intern001.dating.domain.model.Match
import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.domain.model.MatchCriteria
import com.intern001.dating.domain.model.MatchResult
import com.intern001.dating.domain.model.MatchStatus
import com.intern001.dating.domain.model.Photo
import com.intern001.dating.domain.model.Range
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
            displayName = displayName ?: "$firstName $lastName",
            age = age,
            gender = gender,
            avatar = avatar,
            photos =
                    photos?.map { photoResponse ->
                        Photo(
                                url = photoResponse.url,
                                order = photoResponse.order ?: 0,
                                uploadedAt =
                                        photoResponse.uploadedAt?.let {
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
    return UserProfile(
            id = id,
            userId = userId,
            firstName = firstName,
            lastName = lastName,
            displayName = displayName,
            avatar = avatar,
            bio = bio,
            age = age,
            gender = gender,
            interests = interests ?: emptyList(),
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
            occupation = occupation,
            company = company,
            education = education,
            zodiacSign = zodiacSign,
            photos =
                    photos?.map {
                        Photo(
                                url = it.url,
                                order = it.order ?: 0,
                                uploadedAt =
                                        it.uploadedAt?.let { dateStr ->
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
