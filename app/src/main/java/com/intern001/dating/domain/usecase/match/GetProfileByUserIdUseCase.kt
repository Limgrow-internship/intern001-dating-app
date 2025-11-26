package com.intern001.dating.domain.usecase.match

import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.domain.repository.MatchRepository
import javax.inject.Inject

class GetProfileByUserIdUseCase @Inject constructor(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(userId: String): Result<MatchCard> {
        return matchRepository.getProfileByUserId(userId)
    }
}
