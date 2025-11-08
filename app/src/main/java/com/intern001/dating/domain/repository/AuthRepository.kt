package com.intern001.dating.domain.repository

import com.intern001.dating.domain.model.User

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
        deviceToken: String? = null,
    ): Result<String>

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

    fun isLoggedIn(): Boolean

    fun getStoredUser(): User?
}
