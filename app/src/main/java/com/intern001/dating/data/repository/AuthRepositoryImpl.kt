package com.intern001.dating.data.repository

import android.content.Context
import android.net.Uri
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.request.LoginRequest
import com.intern001.dating.data.model.request.SignupRequest
import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.data.model.response.UserData
import com.intern001.dating.domain.model.User
import com.intern001.dating.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject

@Singleton
class AuthRepositoryImpl
@Inject
constructor(
    @Named("datingApi") private val apiService: DatingApiService,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context,
) : AuthRepository {
    private var cachedUser: User? = null

    companion object {
        private const val CLOUDINARY_CLOUD_NAME = "dkkucj4ax"
        private const val CLOUDINARY_UPLOAD_PRESET = "dating_app_preset"
        private const val CLOUDINARY_URL = "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload"
    }

    override suspend fun login(
        email: String,
        password: String,
        deviceToken: String?,
    ): Result<String> {
        return try {
            val request = LoginRequest(email = email, password = password)
            val response = apiService.login(request)

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    tokenManager.saveTokens(
                        accessToken = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken,
                    )

                    authResponse.user?.let { user ->
                        tokenManager.saveUserInfo(
                            userId = user.id,
                            userEmail = user.email,
                        )
                    }

                    Result.success(authResponse.accessToken)
                } else {
                    Result.failure(Exception("Login response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Login failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signup(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        gender: String,
        dateOfBirth: String,
        deviceToken: String?,
    ): Result<String> {
        return try {
            val request =
                SignupRequest(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    gender = gender,
                    dateOfBirth = dateOfBirth,
                )
            val response = apiService.signup(request)

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    android.util.Log.d("AuthRepository", "Signup successful! Saving tokens...")
                    android.util.Log.d("AuthRepository", "Access token: ${authResponse.accessToken.take(20)}...")
                    android.util.Log.d("AuthRepository", "Refresh token: ${authResponse.refreshToken.take(20)}...")

                    tokenManager.saveTokens(
                        accessToken = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken,
                    )

                    // Also save user info if available
                    authResponse.user?.let { user ->
                        android.util.Log.d("AuthRepository", "Saving user info: ${user.email}")
                        tokenManager.saveUserInfo(
                            userId = user.id,
                            userEmail = user.email,
                        )
                    }

                    // Verify token was saved
                    val savedToken = tokenManager.getAccessToken()
                    android.util.Log.d("AuthRepository", "Token saved verification: ${savedToken != null}")

                    Result.success(authResponse.accessToken)
                } else {
                    Result.failure(Exception("Signup response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Signup failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val response = apiService.logout()

            tokenManager.clearTokens()
            cachedUser = null

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            tokenManager.clearTokens()
            cachedUser = null
            Result.success(Unit)
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            cachedUser?.let {
                return Result.success(it)
            }

            if (tokenManager.getAccessToken() == null) {
                return Result.failure(Exception("User not logged in"))
            }

            val response = apiService.getCurrentUserProfile()

            if (response.isSuccessful) {
                val userData = response.body()
                if (userData != null) {
                    val user = userData.toDomainModel()
                    cachedUser = user
                    Result.success(user)
                } else {
                    Result.failure(Exception("User profile response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Failed to fetch user profile: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.getAccessTokenAsync() != null
    }

    override fun getStoredUser(): User? {
        return cachedUser
    }

    override suspend fun updateUserProfile(
        firstName: String?,
        lastName: String?,
        dateOfBirth: String?,
        gender: String?,
        profileImageUrl: String?,
        mode: String?,
        bio: String?,
    ): Result<User> {
        return try {
            val token = tokenManager.getAccessToken()
            android.util.Log.d("AuthRepository", "Update profile - Token exists: ${token != null}, Token: ${token?.take(20)}...")

            if (token == null) {
                android.util.Log.e("AuthRepository", "No token found! User not logged in")
                return Result.failure(Exception("User not logged in. Please login again."))
            }

            val request = UpdateProfileRequest(
                firstName = firstName,
                lastName = lastName,
                dateOfBirth = dateOfBirth,
                gender = gender,
                profileImageUrl = profileImageUrl,
                mode = mode,
                bio = bio,
            )

            android.util.Log.d("AuthRepository", "Sending update profile request: $request")

            val response = apiService.updateUserProfile(request)

            android.util.Log.d("AuthRepository", "Update profile response code: ${response.code()}")

            if (response.isSuccessful) {
                val userData = response.body()
                if (userData != null) {
                    val user = userData.toDomainModel()
                    cachedUser = user
                    android.util.Log.d("AuthRepository", "Profile updated successfully")
                    Result.success(user)
                } else {
                    android.util.Log.e("AuthRepository", "Update profile response body is null")
                    Result.failure(Exception("Update profile response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Update profile failed: ${response.code()} - $errorBody")

                // If token is expired (401), try to refresh and retry
                if (response.code() == 401) {
                    android.util.Log.d("AuthRepository", "Token expired, attempting refresh...")
                    val refreshResult = refreshTokenIfNeeded()

                    if (refreshResult.isSuccess) {
                        val newToken = tokenManager.getAccessToken()
                        android.util.Log.d("AuthRepository", "Token refreshed successfully!")
                        android.util.Log.d("AuthRepository", "New token preview: ${newToken?.take(30)}...")
                        android.util.Log.d("AuthRepository", "Retrying update profile with new token...")

                        val retryResponse = apiService.updateUserProfile(request)
                        android.util.Log.d("AuthRepository", "Retry response code: ${retryResponse.code()}")

                        if (retryResponse.isSuccessful) {
                            val userData = retryResponse.body()
                            if (userData != null) {
                                val user = userData.toDomainModel()
                                cachedUser = user
                                android.util.Log.d("AuthRepository", "Profile updated successfully after retry")
                                return Result.success(user)
                            }
                        } else {
                            val retryErrorBody = retryResponse.errorBody()?.string()
                            android.util.Log.e("AuthRepository", "Retry failed: ${retryResponse.code()} - $retryErrorBody")
                        }
                    } else {
                        android.util.Log.e("AuthRepository", "Token refresh failed, user needs to login again")
                    }
                }

                Result.failure(Exception("Failed to update profile: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Update profile exception", e)
            Result.failure(e)
        }
    }

    private suspend fun refreshTokenIfNeeded(): Result<String> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken == null) {
                return Result.failure(Exception("No refresh token available"))
            }

            android.util.Log.d("AuthRepository", "Calling refresh token API...")
            val response = apiService.refreshToken(
                com.intern001.dating.data.model.response.RefreshTokenRequest(refreshToken),
            )

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    android.util.Log.d("AuthRepository", "New access token received: ${authResponse.accessToken.take(30)}...")
                    android.util.Log.d("AuthRepository", "Saving new tokens to TokenManager...")
                    tokenManager.saveTokens(
                        accessToken = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken,
                    )
                    // Verify token was saved
                    val savedToken = tokenManager.getAccessToken()
                    android.util.Log.d("AuthRepository", "Token saved? ${savedToken != null}, Preview: ${savedToken?.take(30)}...")
                    Result.success(authResponse.accessToken)
                } else {
                    Result.failure(Exception("Refresh token response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Refresh token failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Refresh token failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Refresh token exception", e)
            Result.failure(e)
        }
    }

    private fun UserData.toDomainModel(): User {
        val currentDate = Date()

        return User(
            id = this.id ?: "",
            email = this.email ?: "",
            phoneNumber = null,
            firstName = this.firstName ?: "",
            lastName = this.lastName ?: "",
            displayName = "${this.firstName ?: ""} ${this.lastName ?: ""}".trim(),
            avatar = this.profileImageUrl,
            bio = this.bio,
            age = this.dateOfBirth?.let { calculateAge(it) },
            gender = this.gender,
            interests = emptyList(),
            relationshipMode = this.mode,
            height = null,
            weight = null,
            location = null,
            occupation = null,
            company = null,
            education = null,
            zodiacSign = null,
            isVerified = false,
            photos = emptyList(),
            profileCompleteness = 0,
            profileViews = 0,
            lastLogin = currentDate,
            status = "active",
            createdAt = currentDate,
            updatedAt = currentDate,
        )
    }

    override suspend fun uploadImage(imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("AuthRepository", "Starting image upload for URI: $imageUri")

                // Convert URI to File
                val file = uriToFile(imageUri) ?: return@withContext Result.failure(
                    Exception("Failed to convert URI to file"),
                )

                android.util.Log.d("AuthRepository", "File created: ${file.absolutePath}, size: ${file.length()} bytes")

                // Create multipart request
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                    .addFormDataPart(
                        "file",
                        file.name,
                        file.asRequestBody("image/*".toMediaTypeOrNull()),
                    )
                    .build()

                android.util.Log.d("AuthRepository", "Uploading to Cloudinary: $CLOUDINARY_URL")

                // Make HTTP request to Cloudinary
                val request = Request.Builder()
                    .url(CLOUDINARY_URL)
                    .post(requestBody)
                    .build()

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val response = client.newCall(request).execute()

                android.util.Log.d("AuthRepository", "Response code: ${response.code}")

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    android.util.Log.d("AuthRepository", "Response body: $responseBody")

                    val jsonObject = JSONObject(responseBody ?: "")
                    val secureUrl = jsonObject.getString("secure_url")

                    android.util.Log.d("AuthRepository", "Upload successful: $secureUrl")

                    // Clean up temp file
                    file.delete()

                    Result.success(secureUrl)
                } else {
                    val errorBody = response.body?.string()
                    android.util.Log.e("AuthRepository", "Upload failed: ${response.code}, body: $errorBody")
                    file.delete()
                    Result.failure(Exception("Upload failed: ${response.code} - $errorBody"))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Upload error", e)
                Result.failure(Exception("Upload error: ${e.message}"))
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

    private fun calculateAge(dateOfBirth: String): Int? {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val birthDate = LocalDate.parse(dateOfBirth, formatter)
            val today = LocalDate.now()
            val period = Period.between(birthDate, today)
            period.years
        } catch (e: Exception) {
            null
        }
    }
}
