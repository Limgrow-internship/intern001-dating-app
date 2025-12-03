package com.intern001.dating.domain.repository

import com.intern001.dating.domain.model.Photo

interface PhotoRepository {
    suspend fun getPhotos(): Result<List<Photo>>

    suspend fun getPrimaryPhoto(): Result<Photo>

    suspend fun getPhotoCount(): Result<Int>

    suspend fun uploadPhoto(filePath: String, type: String = "gallery"): Result<Photo>

    suspend fun setPhotoAsPrimary(photoId: String): Result<Photo>

    suspend fun deletePhoto(photoId: String): Result<Unit>

    suspend fun reorderPhotos(photoIds: List<String>): Result<Unit>
}
