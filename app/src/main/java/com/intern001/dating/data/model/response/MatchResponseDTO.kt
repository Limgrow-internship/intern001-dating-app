package com.intern001.dating.data.model.response

import com.google.gson.annotations.SerializedName

data class MatchCardResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
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
    val isVerified: Boolean? = null,
)

/**
 * PhotoResponse can handle both formats:
 * 1. String URL: "https://cloudinary.com/image.jpg"
 * 2. Object: { "url": "...", "order": 0, "uploadedAt": "..." }
 *
 * Note: Gson will deserialize String as PhotoResponse with just url field populated
 */
data class PhotoResponse(
    @SerializedName("url")
    val url: String,
    @SerializedName("order")
    val order: Int? = null,
    @SerializedName("uploadedAt")
    val uploadedAt: String? = null,
    @SerializedName("id")
    val id: String? = null
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

data class UserProfileResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("displayName")
    val displayName: String,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("age")
    val age: Int? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("interests")
    val interests: List<String>? = null,
    @SerializedName("relationshipMode")
    val relationshipMode: String? = null,
    @SerializedName("height")
    val height: Int? = null,
    @SerializedName("weight")
    val weight: Int? = null,
    @SerializedName("location")
    val location: LocationResponse? = null,
    @SerializedName("occupation")
    val occupation: String? = null,
    @SerializedName("company")
    val company: String? = null,
    @SerializedName("education")
    val education: String? = null,
    @SerializedName("zodiacSign")
    val zodiacSign: String? = null,
    @SerializedName("photos")
    val photos: List<PhotoResponse>? = null,
    @SerializedName("profileCompleteness")
    val profileCompleteness: Int? = null,
    @SerializedName("profileViews")
    val profileViews: Int? = null,
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
