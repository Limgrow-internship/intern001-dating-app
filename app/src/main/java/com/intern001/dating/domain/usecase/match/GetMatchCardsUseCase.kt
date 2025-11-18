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
        return try {
            matchRepository.getMatchCards(limit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
