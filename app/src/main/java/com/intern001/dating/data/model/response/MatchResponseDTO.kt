package com.intern001.dating.data.model.response

import com.google.gson.annotations.SerializedName

data class MatchCardResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("displayName")
    val displayName: String? = null,
    @SerializedName("age")
    val age: Int? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("photos")
    val photos: List<PhotoResponse>? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("distance")
    val distance: Double? = null,
    @SerializedName("location")
    val location: LocationResponse? = null,
    @SerializedName("occupation")
    val occupation: String? = null,
    @SerializedName("company")
    val company: String? = null,
    @SerializedName("education")
    val education: String? = null,
    @SerializedName("interests")
    val interests: List<String>? = null,
    @SerializedName("relationshipMode")
    val relationshipMode: String? = null,
    @SerializedName("height")
    val height: Int? = null,
    @SerializedName("zodiacSign")
    val zodiacSign: String? = null,
    @SerializedName("isVerified")
    val isVerified: Boolean? = false,
)

/**
 * PhotoResponse - Matches backend Photo model from NestJS/MongoDB
 * Backend returns full photo objects with metadata
 */
data class PhotoResponse(
    @SerializedName("_id")
    val id: String? = null, // MongoDB _id field
    @SerializedName("id")
    val idAlt: String? = null, // Alternative id field (some endpoints may use this)
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("url")
    val url: String,
    @SerializedName("cloudinaryPublicId")
    val cloudinaryPublicId: String? = null,
    @SerializedName("type")
    val type: String? = null, // 'avatar' | 'gallery' | 'selfie' - default 'gallery'
    @SerializedName("source")
    val source: String? = null, // 'upload' | 'google' | 'facebook' | 'apple' - default 'upload'
    @SerializedName("isPrimary")
    val isPrimary: Boolean? = false,
    @SerializedName("order")
    val order: Int? = 0,
    @SerializedName("isVerified")
    val isVerified: Boolean? = false,
    @SerializedName("width")
    val width: Int? = null,
    @SerializedName("height")
    val height: Int? = null,
    @SerializedName("fileSize")
    val fileSize: Int? = null, // in bytes
    @SerializedName("format")
    val format: String? = null, // 'jpg', 'png', etc.
    @SerializedName("isActive")
    val isActive: Boolean? = true,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
)

data class LocationResponse(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("country")
    val country: String? = null,
)

data class MatchCardsListResponse(
    @SerializedName("cards")
    val cards: List<MatchCardResponse>,
    @SerializedName("hasMore")
    val hasMore: Boolean? = null,
)

data class MatchResultResponse(
    @SerializedName("isMatch")
    val isMatch: Boolean,
    @SerializedName("matchId")
    val matchId: String? = null,
    @SerializedName("matchedUser")
    val matchedUser: UserProfileResponse? = null,
)

/**
 * UserProfileResponse - Matches backend Profile model from NestJS/MongoDB
 */
data class UserProfileResponse(
    @SerializedName("_id")
    val id: String? = null, // MongoDB _id field
    @SerializedName("id")
    val idAlt: String? = null, // Alternative id field
    @SerializedName("userId")
    val userId: String,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("displayName")
    val displayName: String? = null,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("age")
    val age: Int? = null,
    @SerializedName("gender")
    val gender: String? = null, // 'male' | 'female' | 'other'
    @SerializedName("interests")
    val interests: List<String>? = null,
    @SerializedName("mode")
    val mode: String? = null, // 'dating' | 'friend'
    @SerializedName("relationshipMode")
    val relationshipMode: String? = null, // 'serious' | 'casual' | 'friendship'
    @SerializedName("height")
    val height: Int? = null, // in centimeters
    @SerializedName("weight")
    val weight: Int? = null, // in kilograms
    @SerializedName("location")
    val location: LocationResponse? = null,
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("country")
    val country: String? = null,
    @SerializedName("occupation")
    val occupation: String? = null,
    @SerializedName("company")
    val company: String? = null,
    @SerializedName("education")
    val education: String? = null,
    @SerializedName("zodiacSign")
    val zodiacSign: String? = null,
    @SerializedName("verifiedAt")
    val verifiedAt: String? = null,
    @SerializedName("verifiedBadge")
    val verifiedBadge: Boolean? = false,
    @SerializedName("isVerified")
    val isVerified: Boolean? = false,
    @SerializedName("photos")
    val photos: List<PhotoResponse>? = null,
    @SerializedName("profileCompleteness")
    val profileCompleteness: Int? = 0, // 0-100
    @SerializedName("profileViews")
    val profileViews: Int? = 0,
    @SerializedName("goals")
    val goals: List<String>? = null,
    @SerializedName("job")
    val job: String? = null,
    @SerializedName("openQuestionAnswers")
    val openQuestionAnswers: Map<String, String>? = null,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
)

data class MatchResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("matchedUserId")
    val matchedUserId: String,
    @SerializedName("matchedUser")
    val matchedUser: UserProfileResponse,
    @SerializedName("status")
    val status: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("matchedAt")
    val matchedAt: String? = null,
)

data class MatchesListResponse(
    @SerializedName("matches")
    val matches: List<MatchResponse>,
    @SerializedName("total")
    val total: Int? = null,
    @SerializedName("page")
    val page: Int? = null,
    @SerializedName("limit")
    val limit: Int? = null,
)

data class RecommendationCriteriaResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("seekingGender")
    val seekingGender: List<String>? = null,
    @SerializedName("ageRange")
    val ageRange: RangeResponse? = null,
    @SerializedName("distanceRange")
    val distanceRange: RangeResponse? = null,
    @SerializedName("interests")
    val interests: List<String>? = null,
    @SerializedName("relationshipModes")
    val relationshipModes: List<String>? = null,
    @SerializedName("heightRange")
    val heightRange: RangeResponse? = null,
)

data class RangeResponse(
    @SerializedName("min")
    val min: Int,
    @SerializedName("max")
    val max: Int,
)

data class MatchResponseDTO(
    val conversationId: String,
    val matchId: String,
    val lastActivityAt: String,
    val matchedUser: MatchedUserDTO,
)

data class MatchedUserDTO(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val url: String?,
    val age: Int?,
    val city: String?,
)
