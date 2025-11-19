package com.intern001.dating.domain.repository

import com.intern001.dating.domain.model.MatchCriteria
import com.intern001.dating.domain.model.UserPreferences

interface RecommendationRepository {
    suspend fun getRecommendationCriteria(): Result<MatchCriteria>

    suspend fun updateRecommendationCriteria(criteria: MatchCriteria): Result<MatchCriteria>

    suspend fun getUserPreferences(): Result<UserPreferences>

    suspend fun updateUserPreferences(preferences: UserPreferences): Result<UserPreferences>
}
