package com.example.heartondatingapp.domain.usecase.auth

import com.example.heartondatingapp.domain.repository.AuthRepository

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
