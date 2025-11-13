package com.intern001.dating.domain.repository

interface UserRepository {
    suspend fun deleteAccount(): Result<Unit>
}
