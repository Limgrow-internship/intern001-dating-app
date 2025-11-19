package com.intern001.dating.data.repository

import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.domain.model.VerificationResult
import com.intern001.dating.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Named
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

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
