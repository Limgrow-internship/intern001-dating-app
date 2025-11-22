package com.intern001.dating.data.model.response

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("expiresIn")
    val expiresIn: Long? = null,
    @SerializedName("user")
    val user: UserBasicData? = null,
    @SerializedName("message")
    val message: String? = null,
)

data class UserBasicData(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
)

data class UserData(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("firstName")
    val firstName: String? = null,

    @SerializedName("lastName")
    val lastName: String? = null,

    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,

    @SerializedName("gender")
    val gender: String? = null,

    @SerializedName("profilePicture")
    val profilePicture: String? = null,

    @SerializedName("avatar")
    val avatar: String? = null,

    @SerializedName("photos")
    val photos: List<String>? = null,

    @SerializedName("mode")
    val mode: String? = null,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("displayName")
    val displayName: String? = null,

    @SerializedName("interests")
    val interests: List<String>? = null,

    @SerializedName("location")
    val location: LocationData? = null,

    @SerializedName("city")
    val city: String? = null,

    @SerializedName("country")
    val country: String? = null,

    @SerializedName("age")
    val age: Int? = null,

    @SerializedName("verifiedAt")
    val verifiedAt: String? = null,

    @SerializedName("selfieImage")
    val selfieImage: String? = null,

    @SerializedName("verifiedBadge")
    val verifiedBadge: Boolean? = null,

    @SerializedName("occupation")
    val occupation: String? = null,

    @SerializedName("company")
    val company: String? = null,

    @SerializedName("education")
    val education: String? = null,

    @SerializedName("relationshipMode")
    val relationshipMode: String? = null,

    @SerializedName("height")
    val height: Int? = null,

    @SerializedName("weight")
    val weight: Int? = null,

    @SerializedName("zodiacSign")
    val zodiacSign: String? = null,

    @SerializedName("isVerified")
    val isVerified: Boolean? = null,

    @SerializedName("profileCompleteness")
    val profileCompleteness: Int? = null,

    @SerializedName("profileViews")
    val profileViews: Int? = null,

    @SerializedName("goals")
    val goals: String? = null,

    @SerializedName("job")
    val job: String? = null,

    @SerializedName("openQuestionAnswers")
    val openQuestionAnswers: Map<String, String>? = null
)

data class LocationData(
    @SerializedName("type")
    val type: String? = "Point",

    @SerializedName("coordinates")
    val coordinates: List<Double>? = null
)


data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String,
)

data class OtpResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("success")
    val success: Boolean? = null,
)

data class VerifyOtpResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("success")
    val success: Boolean? = null,

    @SerializedName("verified")
    val verified: Boolean? = null,

    @SerializedName("accessToken")
    val accessToken: String? = null,

    @SerializedName("refreshToken")
    val refreshToken: String? = null,

    @SerializedName("user")
    val user: UserBasicData? = null,
)

data class ChangePasswordResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("oldPassword")
    val oldPassword: String? = null,

    @SerializedName("newPassword")
    val newPassword: String? = null,

    @SerializedName("confirmPassword")
    val confirmPassword: String? = null,

    @SerializedName("deviceInfo")
    val deviceInfo: String? = null,
)

data class FacebookLoginResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("user")
    val user: UserBasicData,
    @SerializedName("profile")
    val profile: ProfileData,
    @SerializedName("message")
    val message: String?,
)

data class GoogleLoginResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("user")
    val user: UserBasicData,
    @SerializedName("profile")
    val profile: ProfileData,
    @SerializedName("message")
    val message: String?,
)

data class ProfileData(
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("lastName")
    val lastName: String?,
)

data class VerifyFaceResponse(
    val verified: Boolean,
    val message: String,
)
