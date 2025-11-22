package com.intern001.dating.domain.usecase.auth

import com.intern001.dating.domain.model.UpdateProfile
import com.intern001.dating.domain.model.UserProfile
import com.intern001.dating.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository

) {

    suspend operator fun invoke(): Result<UpdateProfile> {
        return try {
            val result = authRepository.getUserProfile()
            if (result.isSuccess) {
                result
            } else {
                Result.failure(Exception("Failed to get current user profile", result.exceptionOrNull()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

