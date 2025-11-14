package com.intern001.dating.data.repository

import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Named

class UserRepositoryImpl @Inject constructor(
    @Named("datingApi") private val api: DatingApiService,
    private val tokenManager: TokenManager,
) : UserRepository {
    override suspend fun deleteAccount(): Result<Unit> {
        val token = tokenManager.getAccessToken()
        return try {
            val response = api.deleteAccount("Bearer $token")
            if (response.isSuccessful) {
                tokenManager.clearTokens()
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Delete failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
