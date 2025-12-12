package com.intern001.dating.domain.usecase.forgot

import com.intern001.dating.domain.repository.ForgotPasswordRepository
import javax.inject.Inject

class ForgotPasswordUseCase @Inject constructor(
    private val repository: ForgotPasswordRepository,
) {
    suspend fun requestOtp(email: String) {
        repository.requestResetOtp(email)
    }

    suspend fun verifyOtp(email: String, otp: String): String {
        return repository.verifyResetOtp(email, otp)
    }

    suspend fun resetPassword(
        otpToken: String,
        newPassword: String,
        confirmPassword: String,
    ) {
        repository.resetPassword(otpToken, newPassword, confirmPassword)
    }
}
