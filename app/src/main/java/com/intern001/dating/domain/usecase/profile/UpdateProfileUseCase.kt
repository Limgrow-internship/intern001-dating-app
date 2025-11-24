package com.intern001.dating.domain.usecase.profile

import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.domain.model.UpdateProfile
import com.intern001.dating.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(request: UpdateProfileRequest): Result<UpdateProfile> {
        return try {
            // UpdateProfile is a typealias of UserProfile, so we can directly use the result
            val result = authRepository.updateUserProfile(request)
            result.map { it as UpdateProfile }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
