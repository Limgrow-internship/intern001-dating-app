package com.intern001.dating.domain.usecase.match

import com.intern001.dating.domain.repository.MatchRepository
import javax.inject.Inject

class PassUserUseCase
@Inject
constructor(
        private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(targetUserId: String): Result<Unit> {
        if (targetUserId.isBlank()) {
            return Result.failure(IllegalArgumentException("Target user ID cannot be empty"))
        }
        return matchRepository.passUser(targetUserId)
    }
}
