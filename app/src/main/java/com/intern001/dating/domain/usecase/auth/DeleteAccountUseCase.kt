package com.intern001.dating.domain.usecase.auth

import com.intern001.dating.domain.repository.UserRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): Result<Unit> = userRepository.deleteAccount()
}
