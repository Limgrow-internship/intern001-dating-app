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

/**
 * UpdateProfileRequest - Matches backend Profile model fields
 * Note: profilePicture is deprecated - use photo upload API instead
 */
data class UpdateProfileRequest(
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("displayName")
    val displayName: String? = null,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    @SerializedName("gender")
    val gender: String? = null, // 'male' | 'female' | 'other'
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("interests")
    val interests: List<String>? = null,
    @SerializedName("mode")
    val mode: String? = null, // 'dating' | 'friend'
    @SerializedName("relationshipMode")
    val relationshipMode: String? = null, // 'serious' | 'casual' | 'friendship'
    @SerializedName("height")
    val height: Int? = null, // in centimeters (120-220)
    @SerializedName("weight")
    val weight: Int? = null, // in kilograms (30-300)
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
    @SerializedName("goals")
    val goals: String? = null,
    @SerializedName("job")
    val job: String? = null,
    @SerializedName("openQuestionAnswers")
    val openQuestionAnswers: Map<String, String>? = null,
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

// ============================================================
// Photo Management Request DTOs - New Photo System
// ============================================================

data class ReorderPhotosRequest(
    @SerializedName("photoIds")
    val photoIds: List<String>, // Ordered list of photo IDs
)
