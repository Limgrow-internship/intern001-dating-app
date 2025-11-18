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
    val profileImageUrl: String? = null,
    @SerializedName("mode")
    val mode: String? = null,
    @SerializedName("bio")
    val bio: String? = null,
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
