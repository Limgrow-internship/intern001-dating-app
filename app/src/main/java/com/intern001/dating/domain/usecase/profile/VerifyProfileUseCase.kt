package com.intern001.dating.domain.usecase.profile

import com.intern001.dating.domain.repository.UserRepository
import javax.inject.Inject

class VerifyProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun execute(imageBytes: ByteArray) = userRepository.verifyFace(imageBytes)
}
