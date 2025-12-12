package com.intern001.dating.domain.usecase.profile

import com.intern001.dating.domain.model.EnhancedBioResult
import com.intern001.dating.domain.repository.AuthRepository
import javax.inject.Inject

class EnhanceBioUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<EnhancedBioResult> {
        return authRepository.enhanceBio()
    }
}
