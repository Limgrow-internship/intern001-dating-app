package com.example.heartondatingapp.domain.repository

import com.example.heartondatingapp.domain.model.AuthState
import com.example.heartondatingapp.domain.model.User

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
        deviceToken: String? = null,
    ): Result<AuthState>

    suspend fun signup(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        gender: String,
        dateOfBirth: String,
        deviceToken: String? = null,
    ): Result<AuthState>

    suspend fun logout(): Result<Unit>

    suspend fun getCurrentUser(): Result<User>

    fun isLoggedIn(): Boolean

    fun getStoredUser(): User?
}
