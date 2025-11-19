package com.intern001.dating.domain.usecase.recommendation

import com.intern001.dating.domain.model.MatchCriteria
import com.intern001.dating.domain.repository.RecommendationRepository
import javax.inject.Inject

class GetRecommendationCriteriaUseCase
@Inject
constructor(
    private val recommendationRepository: RecommendationRepository,
) {
    suspend operator fun invoke(): Result<MatchCriteria> {
        return try {
            recommendationRepository.getRecommendationCriteria()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
