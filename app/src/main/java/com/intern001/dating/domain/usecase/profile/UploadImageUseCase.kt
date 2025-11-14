package com.intern001.dating.domain.usecase.profile

import android.net.Uri
import com.intern001.dating.domain.repository.AuthRepository
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(imageUri: Uri): Result<String> {
        return authRepository.uploadImage(imageUri)
    }
}
