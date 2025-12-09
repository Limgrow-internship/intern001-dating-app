package com.intern001.dating.data.repository

import android.util.Log
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.prefs.TokenManager
import com.intern001.dating.data.model.request.RecommendationCriteriaRequest
import com.intern001.dating.data.model.response.toMatchCriteria
import com.intern001.dating.domain.model.MatchCriteria
import com.intern001.dating.domain.model.UserPreferences
import com.intern001.dating.domain.repository.RecommendationRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class RecommendationRepositoryImpl
@Inject
constructor(
    @Named("datingApi") private val apiService: DatingApiService,
    private val tokenManager: TokenManager,
) : RecommendationRepository {
    companion object {
        private const val TAG = "RecommendationRepositoryImpl"
    }

    override suspend fun getRecommendationCriteria(): Result<MatchCriteria> {
        return try {
            val response = apiService.getRecommendationCriteria()
            if (response.isSuccessful) {
                val criteria = response.body()?.toMatchCriteria()
                if (criteria != null) {
                    Result.success(criteria)
                } else {
                    Result.failure(Exception("Invalid response from server"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to get recommendation criteria"
                Log.e(TAG, "getRecommendationCriteria error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getRecommendationCriteria exception", e)
            Result.failure(e)
        }
    }

    override suspend fun updateRecommendationCriteria(criteria: MatchCriteria): Result<MatchCriteria> {
        return try {
            val request =
                RecommendationCriteriaRequest(
                    seekingGender = criteria.seekingGender,
                    minAge = criteria.ageRange?.min,
                    maxAge = criteria.ageRange?.max,
                    maxDistance = criteria.distanceRange?.max,
                    interests = criteria.interests,
                    relationshipModes = criteria.relationshipModes,
                    minHeight = criteria.heightRange?.min,
                    maxHeight = criteria.heightRange?.max,
                )
            val response = apiService.updateRecommendationCriteria(request)
            if (response.isSuccessful) {
                val updatedCriteria = response.body()?.toMatchCriteria()
                if (updatedCriteria != null) {
                    Result.success(updatedCriteria)
                } else {
                    Result.failure(Exception("Invalid response from server"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to update recommendation criteria"
                Log.e(TAG, "updateRecommendationCriteria error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateRecommendationCriteria exception", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserPreferences(): Result<UserPreferences> {
        // TODO: Implement when backend API is available
        return Result.failure(UnsupportedOperationException("Not yet implemented"))
    }

    override suspend fun updateUserPreferences(preferences: UserPreferences): Result<UserPreferences> {
        // TODO: Implement when backend API is available
        return Result.failure(UnsupportedOperationException("Not yet implemented"))
    }
}
