package com.intern001.dating.domain.usecase.photo

import android.net.Uri
import com.intern001.dating.data.repository.PhotoRepositoryImpl
import com.intern001.dating.domain.model.Photo
import javax.inject.Inject

class UploadPhotoUseCase @Inject constructor(
    private val photoRepository: PhotoRepositoryImpl,
) {
    suspend operator fun invoke(uri: Uri, type: String = "gallery"): Result<Photo> {
        return try {
            photoRepository.uploadPhotoFromUri(uri, type)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
