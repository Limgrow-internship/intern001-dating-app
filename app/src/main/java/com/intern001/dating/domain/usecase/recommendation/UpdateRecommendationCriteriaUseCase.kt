package com.intern001.dating.domain.usecase.recommendation

import com.intern001.dating.domain.model.MatchCriteria
import com.intern001.dating.domain.repository.RecommendationRepository
import javax.inject.Inject

class UpdateRecommendationCriteriaUseCase
@Inject
constructor(
    private val recommendationRepository: RecommendationRepository,
) {
    suspend operator fun invoke(criteria: MatchCriteria): Result<MatchCriteria> {
        return recommendationRepository.updateRecommendationCriteria(criteria)
    }
}
