package com.intern001.dating.data.repository

import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.request.LoginRequest
import com.intern001.dating.data.model.request.SignupRequest
import com.intern001.dating.data.model.response.UserData
import com.intern001.dating.domain.model.AuthState
import com.intern001.dating.domain.model.User
import com.intern001.dating.domain.repository.AuthRepository
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl
    @Inject
    constructor(
        private val apiService: DatingApiService,
        private val tokenManager: TokenManager,
    ) : AuthRepository {
        private var cachedUser: User? = null

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
                        // Save tokens
                        tokenManager.saveTokens(
                            accessToken = authResponse.accessToken,
                            refreshToken = authResponse.refreshToken,
                        )

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
                        // Save tokens
                        tokenManager.saveTokens(
                            accessToken = authResponse.accessToken,
                            refreshToken = authResponse.refreshToken,
                        )

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
                // Call logout API
                val response = apiService.logout()

                // Clear tokens regardless of API response
                tokenManager.clearTokens()
                cachedUser = null

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    // Still return success since tokens are cleared locally
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                // Clear local data even if API call fails
                tokenManager.clearTokens()
                cachedUser = null
                Result.success(Unit)
            }
        }

        override suspend fun getCurrentUser(): Result<User> {
            return try {
                // Return cached user if available
                cachedUser?.let {
                    return Result.success(it)
                }

                // If no cached user and no token, user is not logged in
                if (tokenManager.getAccessToken() == null) {
                    return Result.failure(Exception("User not logged in"))
                }

                // Fetch user profile from API
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

        override fun isLoggedIn(): Boolean {
            return tokenManager.getAccessToken() != null
        }

        override fun getStoredUser(): User? {
            return cachedUser
        }

        // Extension function to convert UserData (DTO) to User (domain model)
        private fun UserData.toDomainModel(): User {
            val currentDate = Date()

            return User(
                id = this.id,
                email = this.email,
                phoneNumber = null,
                firstName = this.firstName,
                lastName = this.lastName,
                displayName = "${this.firstName} ${this.lastName}",
                avatar = this.profileImageUrl,
                bio = null,
                age = calculateAge(this.dateOfBirth),
                gender = this.gender,
                interests = emptyList(),
                relationshipGoal = null,
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

        /**
         * Calculate age accurately using java.time.Period to handle leap years.
         * @param dateOfBirth Date string in "yyyy-MM-dd" format
         * @return Age in years, or null if parsing fails
         */
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
