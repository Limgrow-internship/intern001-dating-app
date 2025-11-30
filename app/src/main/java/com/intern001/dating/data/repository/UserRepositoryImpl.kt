package com.intern001.dating.data.repository

import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.domain.model.VerificationResult
import com.intern001.dating.domain.repository.AuthRepository
import com.intern001.dating.domain.repository.NotificationRepository
import com.intern001.dating.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Named
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepositoryImpl @Inject constructor(
    @Named("datingApi") private val api: DatingApiService,
    private val tokenManager: TokenManager,
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository,
) : UserRepository {
    override suspend fun deleteAccount(): Result<Unit> {
        val token = tokenManager.getAccessToken()
        return try {
            // First delete profile on server (if endpoint exists)
            try {
                api.deleteProfile("Bearer $token")
            } catch (e: Exception) {
                // If deleteProfile endpoint doesn't exist or fails, continue with account deletion
                // The account deletion might already delete the profile
            }

            // Then delete account on server
            val deleteAccountResponse = api.deleteAccount("Bearer $token")

            // Always clear all user data regardless of API response
            // This ensures local data is deleted even if server deletion fails
            clearAllUserData()

            if (deleteAccountResponse.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(deleteAccountResponse.errorBody()?.string() ?: "Delete account failed"))
            }
        } catch (e: Exception) {
            // Clear data even if exception occurs
            clearAllUserData()
            Result.failure(e)
        }
    }

    private suspend fun clearAllUserData() {
        // Clear profile cache first
        authRepository.clearUserCache()
        // Clear notifications
        notificationRepository.deleteAllNotifications()
        // Clear tokens and user info last
        tokenManager.clearTokens()
    }
    override suspend fun verifyFace(imageBytes: ByteArray): Result<VerificationResult> {
        val selfieRequest = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
        val selfiePart = MultipartBody.Part.createFormData("selfie", "selfie.jpg", selfieRequest)
        return try {
            val response = api.verifyFace(selfiePart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(VerificationResult(response.body()!!.verified, response.body()!!.message))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Verify failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
