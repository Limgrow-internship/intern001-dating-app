package com.intern001.dating.data.repository

import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.response.toDomain
import com.intern001.dating.domain.model.LikedYouUser
import com.intern001.dating.domain.repository.LikedYouRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class LikedYouRepositoryImpl @Inject constructor(
    @Named("datingApi") private val apiService: DatingApiService,
    private val tokenManager: TokenManager,
) : LikedYouRepository {

    companion object {
        private const val TAG = "LikedYouRepositoryImpl"
    }

    override suspend fun getUsersWhoLikedMe(): Result<List<LikedYouUser>> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("User not logged in"))
            }
            val rawList = apiService.getUsersWhoLikedYou()

            val users = rawList.map { it.toDomain() }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
