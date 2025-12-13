package com.intern001.dating.domain.repository

import android.net.Uri
import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.data.model.response.FacebookLoginResponse
import com.intern001.dating.data.model.response.GoogleLoginResponse
import com.intern001.dating.domain.model.EnhancedBioResult
import com.intern001.dating.domain.model.User
import com.intern001.dating.domain.model.UserProfile

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
        deviceId: String? = null,
    ): Result<String>

    suspend fun facebookLogin(
        accessToken: String,
    ): Result<FacebookLoginResponse>

    suspend fun signup(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        gender: String,
        dateOfBirth: String,
        deviceId: String? = null,
    ): Result<String>

    suspend fun logout(): Result<Unit>

    suspend fun getCurrentUser(): Result<User>

    suspend fun getUserProfile(): Result<UserProfile>

    suspend fun updateUserProfile(request: UpdateProfileRequest): Result<UserProfile>

    suspend fun generateBio(prompt: String): Result<UserProfile>

    suspend fun enhanceBio(): Result<EnhancedBioResult>

    suspend fun uploadImage(imageUri: Uri): Result<String>

    suspend fun isLoggedIn(): Boolean

    fun getStoredUser(): User?

    suspend fun googleLogin(
        idToken: String,
    ): Result<GoogleLoginResponse>

    suspend fun clearUserCache()
}
