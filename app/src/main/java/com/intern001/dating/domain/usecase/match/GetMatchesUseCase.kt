package com.intern001.dating.domain.usecase.match

import com.intern001.dating.domain.model.Match
import com.intern001.dating.domain.repository.MatchRepository
import javax.inject.Inject

class GetMatchesUseCase
@Inject
constructor(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20,
    ): Result<List<Match>> {
        return try {
            matchRepository.getMatches(page, limit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
