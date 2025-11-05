package com.intern001.dating.domain.usecase.auth

import com.intern001.dating.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            authRepository.logout()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
