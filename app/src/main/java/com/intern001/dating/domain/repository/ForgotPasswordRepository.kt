package com.intern001.dating.domain.repository

interface ForgotPasswordRepository {
    suspend fun requestResetOtp(email: String)
    suspend fun verifyResetOtp(email: String, otp: String): String
    suspend fun resetPassword(
        otpToken: String,
        newPassword: String,
        confirmPassword: String,
    )
}
