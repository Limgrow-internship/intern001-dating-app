package com.intern001.dating.domain.repository

import com.intern001.dating.domain.model.VerificationResult

interface UserRepository {
    suspend fun deleteAccount(): Result<Unit>
    suspend fun verifyFace(imageBytes: ByteArray): Result<VerificationResult>
}
