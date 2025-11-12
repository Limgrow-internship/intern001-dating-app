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
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("profileImageUrl")
    val profileImageUrl: String? = null,
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
