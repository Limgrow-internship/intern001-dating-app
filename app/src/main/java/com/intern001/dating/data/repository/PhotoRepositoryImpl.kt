package com.intern001.dating.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
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
            var compressedFile: File? = null
            try {
                val token = tokenManager.getAccessToken()
                if (token == null) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }

                val originalFile = File(filePath)
                if (!originalFile.exists()) {
                    return@withContext Result.failure(Exception("File not found: $filePath"))
                }

                // Compress and resize image before upload
                compressedFile = compressImage(originalFile) ?: originalFile
                val fileSizeMB = compressedFile.length() / (1024.0 * 1024.0)
                Log.d("PhotoRepository", "Uploading photo: ${compressedFile.name}, size: ${String.format("%.2f", fileSizeMB)} MB")

                // Create request body for file
                val requestFile = compressedFile.asRequestBody("image/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", compressedFile.name, requestFile)

                // Create request body for type
                val typePart = MultipartBody.Part.createFormData("type", type)

                val response = apiService.uploadPhoto(filePart, typePart)

                // Clean up compressed file if it's different from original
                if (compressedFile != originalFile && compressedFile.exists()) {
                    compressedFile.delete()
                }

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
                // Clean up compressed file on error
                compressedFile?.let { if (it.exists() && it.absolutePath != filePath) it.delete() }
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

            // Read bitmap from URI
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) {
                Log.e("PhotoRepository", "Failed to decode bitmap from URI: $uri")
                return null
            }

            // Compress and resize bitmap (always returns a new bitmap)
            val compressedBitmap = resizeAndCompressBitmap(bitmap)
            bitmap.recycle() // Safe to recycle original now

            // Save compressed bitmap to temp file
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()
            compressedBitmap.recycle() // Recycle compressed bitmap after saving

            tempFile
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error converting URI to file", e)
            null
        }
    }

    private fun compressImage(file: File): File? {
        return try {
            val fileSizeMB = file.length() / (1024.0 * 1024.0)
            Log.d("PhotoRepository", "Original file size: ${String.format("%.2f", fileSizeMB)} MB")

            if (file.length() < 1024 * 1024) {
                return file
            }

            // Decode bitmap with options to reduce memory usage
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            // Calculate sample size to reduce memory
            val sampleSize = calculateInSampleSize(options, 1920, 1920)
            options.inJustDecodeBounds = false
            options.inSampleSize = sampleSize

            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                ?: return file

            // Resize and compress (always returns a new bitmap)
            val compressedBitmap = resizeAndCompressBitmap(bitmap)
            bitmap.recycle() // Safe to recycle original now

            // Save to temp file
            val compressedFile = File.createTempFile("compressed_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(compressedFile)
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()
            compressedBitmap.recycle() // Recycle compressed bitmap after saving

            val compressedSizeMB = compressedFile.length() / (1024.0 * 1024.0)
            Log.d("PhotoRepository", "Compressed file size: ${String.format("%.2f", compressedSizeMB)} MB")

            compressedFile
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error compressing image", e)
            file
        }
    }

    private fun resizeAndCompressBitmap(bitmap: Bitmap, maxWidth: Int = 1920, maxHeight: Int = 1920): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // If image is already smaller than max dimensions, create a copy
        if (width <= maxWidth && height <= maxHeight) {
            val config = bitmap.config ?: Bitmap.Config.ARGB_8888
            return bitmap.copy(config, false)
        }

        // Calculate new dimensions maintaining aspect ratio
        val scale = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
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
