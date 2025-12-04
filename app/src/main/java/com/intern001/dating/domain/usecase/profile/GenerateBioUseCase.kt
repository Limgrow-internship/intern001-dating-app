package com.intern001.dating.domain.usecase.profile

import com.intern001.dating.domain.model.UpdateProfile
import com.intern001.dating.domain.repository.AuthRepository
import javax.inject.Inject

class GenerateBioUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(prompt: String): Result<UpdateProfile> {
        return authRepository.generateBio(prompt).map { it as UpdateProfile }
    }
}
