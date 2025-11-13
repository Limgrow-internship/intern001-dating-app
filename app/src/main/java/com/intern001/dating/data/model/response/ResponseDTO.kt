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
    @SerializedName("profileImageUrl")
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
