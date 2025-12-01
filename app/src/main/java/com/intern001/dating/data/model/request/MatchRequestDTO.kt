package com.intern001.dating.data.model.request

import com.google.gson.annotations.SerializedName

data class MatchActionRequest(
    @SerializedName("targetUserId")
    val targetUserId: String,
)

data class BlockUserRequest(
    @SerializedName("targetUserId")
    val targetUserId: String,
    @SerializedName("reason")
    val reason: String? = null,
)

data class UnmatchRequest(
    @SerializedName("matchId")
    val matchId: String,
)
data class UnmatchUserRequest(val targetUserId: String)
data class RecommendationCriteriaRequest(
    @SerializedName("seekingGender")
    val seekingGender: List<String>? = null,
    @SerializedName("minAge")
    val minAge: Int? = null,
    @SerializedName("maxAge")
    val maxAge: Int? = null,
    @SerializedName("maxDistance")
    val maxDistance: Int? = null,
    @SerializedName("interests")
    val interests: List<String>? = null,
    @SerializedName("relationshipModes")
    val relationshipModes: List<String>? = null,
    @SerializedName("minHeight")
    val minHeight: Int? = null,
    @SerializedName("maxHeight")
    val maxHeight: Int? = null,
)
