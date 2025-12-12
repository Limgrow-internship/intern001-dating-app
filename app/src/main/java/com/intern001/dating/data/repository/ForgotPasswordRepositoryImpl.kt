package com.intern001.dating.data.repository

import android.content.Context
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.model.request.RequestOtpRequestForgot
import com.intern001.dating.data.model.request.ResetPasswordRequest
import com.intern001.dating.data.model.request.VerifyOtpRequestForgot
import com.intern001.dating.domain.repository.ForgotPasswordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named

class ForgotPasswordRepositoryImpl @Inject constructor(
    @Named("datingApi") private val api: DatingApiService,
    @ApplicationContext private val context: Context,
) : ForgotPasswordRepository {

    override suspend fun requestResetOtp(email: String) {
        val response = api.requestResetOtp(RequestOtpRequestForgot(email))

        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Request OTP failed")
        }
    }

    override suspend fun verifyResetOtp(email: String, otp: String): String {
        val response = api.verifyResetOtp(
            VerifyOtpRequestForgot(email, otp),
        )

        if (!response.isSuccessful || response.body() == null) {
            throw Exception(response.errorBody()?.string() ?: "OTP invalid")
        }

        return response.body()!!.otpToken
    }

    override suspend fun resetPassword(
        otpToken: String,
        newPassword: String,
        confirmPassword: String,
    ) {
        val response = api.resetPassword(
            token = "Bearer $otpToken",
            body = ResetPasswordRequest(newPassword, confirmPassword),
        )

        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Reset password failed")
        }
    }
}
