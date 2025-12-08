package com.intern001.dating.data.repository

import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.response.PhotoResponse
import com.intern001.dating.domain.repository.AuthRepository
import com.intern001.dating.domain.repository.NotificationRepository
import com.intern001.dating.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Named
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UserRepositoryImpl @Inject constructor(
    @Named("datingApi") private val api: DatingApiService,
    private val tokenManager: TokenManager,
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository,
) : UserRepository {
    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            // First delete profile on server (if endpoint exists)
            try {
                api.deleteProfile()
            } catch (e: Exception) {
                // If deleteProfile endpoint doesn't exist or fails, continue with account deletion
                // The account deletion might already delete the profile
            }

            // Then delete account on server
            val deleteAccountResponse = api.deleteAccount()

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

    override suspend fun uploadSelfie(imageBytes: ByteArray): Result<PhotoResponse> {
        val selfieRequest = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val selfiePart = MultipartBody.Part.createFormData("file", "selfie.jpg", selfieRequest)
        val typePart = MultipartBody.Part.createFormData("type", "selfie")
        return try {
            val response = api.uploadPhoto(selfiePart, typePart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Cannot upload selfie"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
