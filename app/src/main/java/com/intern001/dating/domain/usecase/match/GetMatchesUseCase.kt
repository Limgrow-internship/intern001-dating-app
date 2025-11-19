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
        if (page <= 0) {
            return Result.failure(IllegalArgumentException("Page must be greater than 0"))
        }
        if (limit <= 0) {
            return Result.failure(IllegalArgumentException("Limit must be greater than 0"))
        }
        return matchRepository.getMatches(page, limit)
    }
}
