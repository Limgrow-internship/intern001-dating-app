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
