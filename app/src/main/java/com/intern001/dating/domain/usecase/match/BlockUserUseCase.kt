package com.intern001.dating.domain.usecase.match

import com.intern001.dating.domain.repository.MatchRepository
import javax.inject.Inject

class BlockUserUseCase
@Inject
constructor(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        targetUserId: String,
    ): Result<Unit> {
        return try {
            if (targetUserId.isBlank()) {
                return Result.failure(IllegalArgumentException("Target user ID cannot be empty"))
            }
            matchRepository.blockUser(targetUserId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
