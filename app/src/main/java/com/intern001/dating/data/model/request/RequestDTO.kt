package com.intern001.dating.data.model.request

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("deviceId")
    val deviceId: String? = null,
)

data class GoogleLoginRequest(
    @SerializedName("accessToken")
    val accessToken: String,
)

data class SignupRequest(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("deviceId")
    val deviceId: String? = null,
)

data class RequestOtpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
)

data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp")
    val otp: String,
)

data class UpdateProfileRequest(
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
    val location: LocationRequest? = null,

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

data class LocationRequest(
    @SerializedName("type")
    val type: String? = "Point",

    @SerializedName("coordinates")
    val coordinates: List<Double>? = null
)

data class ChangePasswordRequest(
    @SerializedName("oldPassword")
    val oldPassword: String? = null,

    @SerializedName("newPassword")
    val newPassword: String? = null,

    @SerializedName("confirmPassword")
    val confirmPassword: String? = null,

    @SerializedName("deviceInfo")
    val deviceInfo: String? = null,
)

data class FacebookLoginRequest(
    @SerializedName("accessToken")
    val accessToken: String,
)
