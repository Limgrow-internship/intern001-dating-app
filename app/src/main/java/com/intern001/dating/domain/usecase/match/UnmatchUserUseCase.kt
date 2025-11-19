package com.intern001.dating.domain.usecase.match

import com.intern001.dating.domain.repository.MatchRepository
import javax.inject.Inject

class UnmatchUserUseCase
@Inject
constructor(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(matchId: String): Result<Unit> {
        return try {
            if (matchId.isBlank()) {
                return Result.failure(IllegalArgumentException("Match ID cannot be empty"))
            }
            matchRepository.unmatch(matchId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
