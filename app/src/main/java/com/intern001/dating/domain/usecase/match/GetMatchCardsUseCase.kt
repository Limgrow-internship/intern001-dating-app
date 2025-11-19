package com.intern001.dating.domain.usecase.match

import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.domain.repository.MatchRepository
import javax.inject.Inject

class GetMatchCardsUseCase
@Inject
constructor(
        private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(limit: Int = 10): Result<List<MatchCard>> {
        if (limit <= 0) {
            return Result.failure(IllegalArgumentException("Limit must be greater than 0"))
        }
        return matchRepository.getMatchCards(limit)
    }
}
