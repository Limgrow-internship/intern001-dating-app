package com.intern001.dating.data.repository

import android.content.Context
import android.net.Uri
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.request.FacebookLoginRequest
import com.intern001.dating.data.model.request.GenerateBioDto
import com.intern001.dating.data.model.request.GoogleLoginRequest
import com.intern001.dating.data.model.request.LocationRequest
import com.intern001.dating.data.model.request.LoginRequest
import com.intern001.dating.data.model.request.SignupRequest
import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.data.model.response.FacebookLoginResponse
import com.intern001.dating.data.model.response.GoogleLoginResponse
import com.intern001.dating.data.model.response.LocationResponse
import com.intern001.dating.data.model.response.UserData
import com.intern001.dating.data.service.FCMService
import com.intern001.dating.data.service.NotificationService
import com.intern001.dating.domain.model.User
import com.intern001.dating.domain.model.UserLocation
import com.intern001.dating.domain.model.UserProfile
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    private val notificationService: NotificationService,
    private val fcmService: FCMService,
    private val notificationRepository: com.intern001.dating.domain.repository.NotificationRepository,
) : AuthRepository {
    private var cachedUser: User? = null
    private var cachedUserProfile: UserProfile? = null

    companion object {
        private const val CLOUDINARY_CLOUD_NAME = "dkkucj4ax"
        private const val CLOUDINARY_UPLOAD_PRESET = "dating_app_preset"
        private const val CLOUDINARY_URL = "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload"
    }

    override suspend fun login(
        email: String,
        password: String,
        deviceId: String?,
    ): Result<String> {
        return try {
            // Get FCM token to use as deviceToken (FCM token is saved to DB separately via updateFCMToken API)
            // Try to get FCM token with retry (FCM might not be ready immediately)
            var fcmToken: String? = null
            try {
                fcmToken = fcmService.getToken()
            } catch (e: Exception) {
                // Retry once after a short delay
                try {
                    kotlinx.coroutines.delay(500)
                    fcmToken = fcmService.getToken()
                } catch (e2: Exception) {
                    android.util.Log.e("AuthRepository", "Failed to get FCM token after retry: ${e2.message}")
                }
            }

            // Use provided deviceId, or FCM token, or null
            val finalDeviceId = deviceId ?: fcmToken

            val request = LoginRequest(email = email, password = password, deviceId = finalDeviceId)
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

                    // Send FCM token to server after successful login
                    CoroutineScope(Dispatchers.IO).launch {
                        notificationService.initializeFCMToken()
                    }

                    Result.success(authResponse.accessToken)
                } else {
                    android.util.Log.e("AuthRepository", "Login response body is null")
                    Result.failure(Exception("Login response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Login failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Login failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun facebookLogin(accessToken: String): Result<FacebookLoginResponse> {
        return try {
            val response = apiService.facebookLogin(FacebookLoginRequest(accessToken))
            tokenManager.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
            )
            tokenManager.saveUserInfo(
                userId = response.user.id,
                userEmail = response.user.email,
            )
            // Send FCM token to server after successful login (for social login, token is sent separately)
            CoroutineScope(Dispatchers.IO).launch {
                notificationService.initializeFCMToken()
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun googleLogin(accessToken: String): Result<GoogleLoginResponse> {
        return try {
            val response = apiService.googleLogin(GoogleLoginRequest(accessToken))

            tokenManager.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
            )

            tokenManager.saveUserInfo(
                userId = response.user.id,
                userEmail = response.user.email,
            )

            CoroutineScope(Dispatchers.IO).launch {
                notificationService.initializeFCMToken()
            }

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearUserCache() {
        cachedUser = null
        cachedUserProfile = null
    }

    override suspend fun signup(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        gender: String,
        dateOfBirth: String,
        deviceId: String?,
    ): Result<String> {
        return try {
            // Get FCM token to use as deviceToken (FCM token is saved to DB separately via updateFCMToken API)
            // Try to get FCM token with retry (FCM might not be ready immediately)
            var fcmToken: String? = null
            try {
                fcmToken = fcmService.getToken()
            } catch (e: Exception) {
                // Retry once after a short delay
                try {
                    kotlinx.coroutines.delay(500)
                    fcmToken = fcmService.getToken()
                } catch (e2: Exception) {
                    android.util.Log.e("AuthRepository", "Failed to get FCM token after retry: ${e2.message}")
                }
            }

            // Use provided deviceId, or FCM token, or null
            val finalDeviceId = deviceId ?: fcmToken

            val request =
                SignupRequest(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    gender = gender,
                    dateOfBirth = dateOfBirth,
                    deviceId = finalDeviceId,
                )
            val response = apiService.signup(request)

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    tokenManager.saveTokens(
                        accessToken = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken,
                    )

                    // Also save user info if available
                    authResponse.user?.let { user ->
                        tokenManager.saveUserInfo(
                            userId = user.id,
                            userEmail = user.email,
                        )
                    }

                    // Clear all notifications for new account
                    CoroutineScope(Dispatchers.IO).launch {
                        notificationRepository.deleteAllNotifications()
                        notificationService.initializeFCMToken()
                    }

                    Result.success(authResponse.accessToken)
                } else {
                    android.util.Log.e("AuthRepository", "Signup response body is null")
                    Result.failure(Exception("Signup response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Signup failed: ${response.code()} - $errorBody")
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
            cachedUserProfile = null

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            tokenManager.clearTokens()
            cachedUser = null
            cachedUserProfile = null
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
                    val user = userData.toUserModel()
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

    override suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            // Always fetch from server to get latest photos
            // Cache is only used as fallback if API call fails
            if (tokenManager.getAccessToken() == null) {
                // If no token, try cache as fallback
                cachedUserProfile?.let {
                    return Result.success(it)
                }
                return Result.failure(Exception("User not logged in"))
            }

            val response = apiService.getCurrentUserProfile()

            if (response.isSuccessful) {
                val userData = response.body()
                if (userData != null) {
                    val userProfile = userData.toUserProfileModel()
                    cachedUserProfile = userProfile
                    Result.success(userProfile)
                } else {
                    // If API fails, try cache as fallback
                    cachedUserProfile?.let {
                        return Result.success(it)
                    }
                    Result.failure(Exception("User profile response body is null"))
                }
            } else {
                // If API fails, try cache as fallback
                cachedUserProfile?.let {
                    return Result.success(it)
                }
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Failed to fetch user profile: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            // If exception, try cache as fallback
            cachedUserProfile?.let {
                return Result.success(it)
            }
            Result.failure(e)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.getAccessTokenAsync() != null
    }

    override fun getStoredUser(): User? {
        return cachedUser
    }

    override suspend fun updateUserProfile(request: UpdateProfileRequest): Result<UserProfile> {
        return try {
            val token = tokenManager.getAccessToken()
            android.util.Log.d("AuthRepository", "Update profile - Token exists: ${token != null}, Token: ${token?.take(20)}...")

            if (token == null) {
                android.util.Log.e("AuthRepository", "No token found! User not logged in")
                return Result.failure(Exception("User not logged in. Please login again."))
            }

            android.util.Log.d("AuthRepository", "Sending update profile request: $request")
            request.location?.let { loc ->
                android.util.Log.d(
                    "AuthRepository",
                    "Update profile location payload -> lat=${loc.latitude}, lng=${loc.longitude}, city=${loc.city}, country=${loc.country}, coords=${loc.coordinates}",
                )
            } ?: android.util.Log.d("AuthRepository", "Update profile without explicit location payload")

            val response = apiService.updateUserProfile(request)

            android.util.Log.d("AuthRepository", "Update profile response code: ${response.code()}")

            if (response.isSuccessful) {
                val userData = response.body()
                if (userData != null) {
                    val finalUserData = resolveLocationAfterUpdate(userData, request)
                    val userProfile = finalUserData.toUserProfileModel()
                    cachedUserProfile = userProfile
                    finalUserData.location?.let { loc ->
                        android.util.Log.d(
                            "AuthRepository",
                            "Server profile location -> lat=${loc.latitude}, lng=${loc.longitude}, city=${loc.city}, country=${loc.country}",
                        )
                    } ?: android.util.Log.d("AuthRepository", "Server profile returned without location payload")
                    android.util.Log.d("AuthRepository", "Profile updated successfully")
                    Result.success(userProfile)
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
                                val finalUserData = resolveLocationAfterUpdate(userData, request)
                                val userProfile = finalUserData.toUserProfileModel()
                                cachedUserProfile = userProfile
                                finalUserData.location?.let { loc ->
                                    android.util.Log.d(
                                        "AuthRepository",
                                        "Server profile location after retry -> lat=${loc.latitude}, lng=${loc.longitude}, city=${loc.city}, country=${loc.country}",
                                    )
                                } ?: android.util.Log.d("AuthRepository", "Server retry profile returned without location payload")
                                android.util.Log.d("AuthRepository", "Profile updated successfully after retry")
                                return Result.success(userProfile)
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

    override suspend fun generateBio(prompt: String): Result<UserProfile> {
        return try {
            if (tokenManager.getAccessToken() == null) {
                return Result.failure(Exception("User not logged in"))
            }

            val request = GenerateBioDto(prompt = prompt)
            val response = apiService.generateBio(request)

            if (response.isSuccessful) {
                val bioResponse = response.body()
                if (bioResponse != null) {
                    android.util.Log.d("AuthRepository", "Bio generated: ${bioResponse.generatedBio}")
                    android.util.Log.d("AuthRepository", "Provider: ${bioResponse.provider}")

                    // API chỉ generate bio, chưa save vào profile
                    // Cần update profile với bio mới
                    val updateRequest = UpdateProfileRequest(bio = bioResponse.generatedBio)
                    val updateResult = updateUserProfile(updateRequest)

                    updateResult.onSuccess { profile ->
                        android.util.Log.d("AuthRepository", "Bio saved to profile successfully")
                    }

                    updateResult
                } else {
                    Result.failure(Exception("Generate bio response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Generate bio failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to generate bio: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Generate bio exception", e)
            Result.failure(e)
        }
    }

    override suspend fun enhanceBio(): Result<UserProfile> {
        return try {
            if (tokenManager.getAccessToken() == null) {
                return Result.failure(Exception("User not logged in"))
            }

            // Gọi API enhance-bio (không cần body, backend tự lấy bio hiện tại)
            val response = apiService.enhanceBio()

            if (response.isSuccessful) {
                val bioResponse = response.body()
                if (bioResponse != null) {
                    android.util.Log.d("AuthRepository", "Bio enhanced: ${bioResponse.generatedBio}")
                    android.util.Log.d("AuthRepository", "Provider: ${bioResponse.provider}")

                    // API chỉ enhance bio, chưa save vào profile
                    // Cần update profile với bio mới
                    val updateRequest = UpdateProfileRequest(bio = bioResponse.generatedBio)
                    val updateResult = updateUserProfile(updateRequest)

                    updateResult.onSuccess { profile ->
                        android.util.Log.d("AuthRepository", "Enhanced bio saved to profile successfully")
                    }

                    updateResult
                } else {
                    Result.failure(Exception("Enhance bio response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Enhance bio failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to enhance bio: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Enhance bio exception", e)
            Result.failure(e)
        }
    }

    private suspend fun resolveLocationAfterUpdate(
        responseData: UserData,
        request: UpdateProfileRequest,
    ): UserData {
        val hasValidLocation =
            responseData.location?.let { loc ->
                !loc.latitude.isNaN() && !loc.longitude.isNaN()
            } ?: false

        if (hasValidLocation) return responseData

        val requestLocationPayload = request.location?.takeIf { it.hasUsableLocationPayload() }?.toLocationResponse()
        if (requestLocationPayload != null) {
            return responseData.copy(location = requestLocationPayload)
        }

        return fetchProfileDataOrFallback(responseData)
    }

    private suspend fun fetchProfileDataOrFallback(fallback: UserData): UserData {
        return try {
            val profileResponse = apiService.getCurrentUserProfile()
            if (profileResponse.isSuccessful) {
                profileResponse.body() ?: fallback
            } else {
                fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }

    private fun LocationRequest.hasUsableLocationPayload(): Boolean {
        val coordsUsable = coordinates.size >= 2 && coordinates.none { it.isNaN() }
        val latLngUsable = latitude != null && longitude != null
        return coordsUsable || latLngUsable
    }

    private fun LocationRequest.toLocationResponse(): LocationResponse {
        val coords = coordinates.takeIf { it.size >= 2 && it.none(Double::isNaN) }
        val finalLat = latitude ?: coords?.getOrNull(1)
        val finalLng = longitude ?: coords?.getOrNull(0)

        require(finalLat != null && finalLng != null) { "Invalid location payload" }

        return LocationResponse(
            latitude = finalLat,
            longitude = finalLng,
            city = city,
            country = country,
        )
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

    private fun UserData.toUserModel(): User {
        val currentDate = Date()

        return User(
            id = this.id ?: "",
            email = this.email ?: "",
            phoneNumber = null,
            isVerified = false,
            status = "active",
            lastLogin = currentDate,
            createdAt = currentDate,
            updatedAt = currentDate,
        )
    }

    private fun UserData.toUserProfileModel(): UserProfile {
        val currentDate = Date()
        val userId = this.id ?: ""

        // Convert PhotoResponse list to Photo domain models
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }

        val photoList = this.photos?.mapNotNull { photoResponse ->
            // Get ID from either _id or id field (backend may use either)
            val photoId = photoResponse.id ?: photoResponse.idAlt ?: "photo_${System.currentTimeMillis()}_${photoResponse.order ?: 0}"
            com.intern001.dating.domain.model.Photo(
                id = photoId,
                userId = photoResponse.userId,
                url = photoResponse.url,
                cloudinaryPublicId = photoResponse.cloudinaryPublicId,
                type = photoResponse.type ?: "gallery",
                source = photoResponse.source ?: "upload",
                isPrimary = photoResponse.isPrimary ?: false,
                order = photoResponse.order ?: 0,
                isVerified = photoResponse.isVerified ?: false,
                width = photoResponse.width,
                height = photoResponse.height,
                fileSize = photoResponse.fileSize,
                format = photoResponse.format,
                isActive = photoResponse.isActive ?: true,
                createdAt = photoResponse.createdAt?.let {
                    try {
                        dateFormat.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                },
                updatedAt = photoResponse.updatedAt?.let {
                    try {
                        dateFormat.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            )
        } ?: emptyList()

        return UserProfile(
            id = userId, // Profile ID same as User ID for now
            userId = userId,
            firstName = this.firstName,
            lastName = this.lastName,
            displayName = when {
                !this.firstName.isNullOrBlank() && !this.lastName.isNullOrBlank() -> "${this.firstName} ${this.lastName}".trim()
                !this.firstName.isNullOrBlank() -> this.firstName
                !this.lastName.isNullOrBlank() -> this.lastName
                else -> "Unknown"
            },
            dateOfBirth = this.dateOfBirth?.let {
                try {
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(it)
                } catch (e: Exception) {
                    null
                }
            },
            avatar = this.avatar, // Use new avatar field instead of profileImageUrl
            bio = this.bio,
            age = this.dateOfBirth?.let { dobString ->
                try {
                    calculateAge(dobString)
                } catch (e: Exception) {
                    null
                }
            },
            gender = this.gender,
            interests = this.interests ?: emptyList(),
            mode = this.mode,
            relationshipMode = null, // Not in UserData response
            height = this.height,
            weight = this.weight,
            location = this.location?.let {
                UserLocation(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    city = it.city,
                    country = it.country,
                )
            },
            city = this.city,
            country = this.country,
            occupation = this.occupation,
            company = this.company,
            education = this.education,
            zodiacSign = this.zodiacSign,
            job = this.job,
            verifiedAt = null,
            verifiedBadge = false,
            isVerified = false,
            photos = photoList, // Use new photos array
            profileCompleteness = 0,
            profileViews = 0,
            goals = this.goals ?: emptyList(), // goals is now List<String>, not nullable
            openQuestionAnswers = this.openQuestionAnswers ?: emptyMap(),
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
