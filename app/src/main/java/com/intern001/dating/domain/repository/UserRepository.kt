package com.intern001.dating.domain.repository

import com.intern001.dating.data.model.response.PhotoResponse

interface UserRepository {
    suspend fun deleteAccount(): Result<Unit>
    suspend fun uploadSelfie(imageBytes: ByteArray): Result<PhotoResponse>
}
