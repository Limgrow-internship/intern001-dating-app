package com.intern001.dating.domain.repository

import android.net.Uri
import com.intern001.dating.data.model.response.FacebookLoginResponse
import com.intern001.dating.data.model.response.GoogleLoginResponse
import com.intern001.dating.domain.model.User
import com.intern001.dating.domain.model.UserProfile

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
        deviceToken: String? = null,
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
        deviceToken: String? = null,
    ): Result<String>

    suspend fun logout(): Result<Unit>

    suspend fun getCurrentUser(): Result<User>

    suspend fun getUserProfile(): Result<UserProfile>

    suspend fun updateUserProfile(
        firstName: String? = null,
        lastName: String? = null,
        dateOfBirth: String? = null,
        gender: String? = null,
        profileImageUrl: String? = null,
        mode: String? = null,
        bio: String? = null,
    ): Result<UserProfile>

    suspend fun uploadImage(imageUri: Uri): Result<String>

    suspend fun isLoggedIn(): Boolean

    fun getStoredUser(): User?

    suspend fun googleLogin(
        accessToken: String,
    ): Result<GoogleLoginResponse>
}
