package com.intern001.dating.domain.usecase.match

import com.intern001.dating.domain.model.MatchResult
import com.intern001.dating.domain.repository.MatchRepository
import javax.inject.Inject

class LikeUserUseCase
@Inject
constructor(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(targetUserId: String): Result<MatchResult> {
        return try {
            if (targetUserId.isBlank()) {
                return Result.failure(IllegalArgumentException("Target user ID cannot be empty"))
            }
            matchRepository.likeUser(targetUserId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
