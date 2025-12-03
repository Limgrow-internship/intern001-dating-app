package com.intern001.dating.data.repository

import android.content.Context
import android.net.Uri
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.request.ReorderPhotosRequest
import com.intern001.dating.data.model.response.PhotoResponse
import com.intern001.dating.domain.model.Photo
import com.intern001.dating.domain.repository.PhotoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    @Named("datingApi") private val apiService: DatingApiService,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context,
) : PhotoRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override suspend fun getPhotos(): Result<List<Photo>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAccessToken()
                if (token == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }

                val response = apiService.getPhotos()

                if (response.isSuccessful) {
                    val photoListResponse = response.body()
                    if (photoListResponse != null) {
                        val photos = photoListResponse.photos.map { it.toPhoto() }
                        Result.success(photos)
                    } else {
                        Result.failure(Exception("Photo list response body is null"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("Failed to get photos: ${response.code()} - $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getPrimaryPhoto(): Result<Photo> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAccessToken()
                if (token == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }

                val response = apiService.getPrimaryPhoto()

                if (response.isSuccessful) {
                    val photoResponse = response.body()
                    if (photoResponse != null) {
                        Result.success(photoResponse.toPhoto())
                    } else {
                        Result.failure(Exception("Primary photo response body is null"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("Failed to get primary photo: ${response.code()} - $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getPhotoCount(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAccessToken()
                if (token == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }

                val response = apiService.getPhotoCount()

                if (response.isSuccessful) {
                    val countResponse = response.body()
                    if (countResponse != null) {
                        Result.success(countResponse.count)
                    } else {
                        Result.failure(Exception("Photo count response body is null"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("Failed to get photo count: ${response.code()} - $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun uploadPhoto(filePath: String, type: String): Result<Photo> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAccessToken()
                if (token == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }

                val file = File(filePath)
                if (!file.exists()) {
                    return@withContext Result.failure(Exception("File not found: $filePath"))
                }

                // Create request body for file
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // Create request body for type
                val typePart = MultipartBody.Part.createFormData("type", type)

                val response = apiService.uploadPhoto(filePart, typePart)

                if (response.isSuccessful) {
                    val photoResponse = response.body()
                    if (photoResponse != null) {
                        Result.success(photoResponse.toPhoto())
                    } else {
                        Result.failure(Exception("Upload photo response body is null"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("Failed to upload photo: ${response.code()} - $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun uploadPhotoFromUri(uri: Uri, type: String = "gallery"): Result<Photo> {
        return withContext(Dispatchers.IO) {
            try {
                val file = uriToFile(uri) ?: return@withContext Result.failure(
                    Exception("Failed to convert URI to file"),
                )

                val result = uploadPhoto(file.absolutePath, type)

                // Clean up temp file
                file.delete()

                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            tempFile
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun setPhotoAsPrimary(photoId: String): Result<Photo> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAccessToken()
                if (token == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }

                val response = apiService.setPhotoAsPrimary(photoId)

                if (response.isSuccessful) {
                    val photoResponse = response.body()
                    if (photoResponse != null) {
                        Result.success(photoResponse.toPhoto())
                    } else {
                        Result.failure(Exception("Set primary photo response body is null"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("Failed to set primary photo: ${response.code()} - $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deletePhoto(photoId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAccessToken()
                if (token == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }

                val response = apiService.deletePhoto(photoId)

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("Failed to delete photo: ${response.code()} - $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun reorderPhotos(photoIds: List<String>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAccessToken()
                if (token == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }

                val request = ReorderPhotosRequest(photoIds = photoIds)
                val response = apiService.reorderPhotos(request)

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("Failed to reorder photos: ${response.code()} - $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun PhotoResponse.toPhoto(): Photo {
        // Get ID from either _id or id field (MongoDB may use _id)
        val photoId = idAlt ?: id ?: "photo_${System.currentTimeMillis()}"

        return Photo(
            id = photoId,
            userId = userId,
            url = url ?: "",
            cloudinaryPublicId = cloudinaryPublicId,
            type = type ?: "gallery",
            source = source ?: "upload",
            isPrimary = isPrimary ?: false,
            order = order ?: 0,
            isVerified = isVerified ?: false,
            width = width,
            height = height,
            fileSize = fileSize,
            format = format,
            isActive = isActive ?: true,
            createdAt = createdAt?.let {
                try {
                    dateFormat.parse(it)
                } catch (e: Exception) {
                    null
                }
            },
            updatedAt = updatedAt?.let {
                try {
                    dateFormat.parse(it)
                } catch (e: Exception) {
                    null
                }
            },
        )
    }
}
