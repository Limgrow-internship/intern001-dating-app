package com.intern001.dating.presentation.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.request.RequestOtpRequest
import com.intern001.dating.data.model.request.VerifyOtpRequest
import com.intern001.dating.data.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class InfoViewModel @Inject constructor(
    @Named("datingApi") private val apiService: DatingApiService,
    private val tokenManager: TokenManager,
    private val notificationService: NotificationService,
) : ViewModel() {

    fun sendOtp(email: String, password: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.requestOtp(RequestOtpRequest(email, password))
                if (response.isSuccessful) {
                    onResult(response.body()?.message ?: "Success")
                } else {
                    onResult("Failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                onResult("Error: ${e.message}")
            }
        }
    }

    fun verifyOtpAndLogin(
        email: String,
        otp: String,
        password: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                // Step 1: Verify OTP
                val verifyRequest = VerifyOtpRequest(email, otp)
                val verifyResponse = apiService.verifyOtp(verifyRequest)

                if (!verifyResponse.isSuccessful) {
                    val errorBody = verifyResponse.errorBody()?.string()
                    android.util.Log.e("InfoViewModel", "OTP verification failed: $errorBody")
                    onResult(false, "OTP verification failed")
                    return@launch
                }

                val responseBody = verifyResponse.body()

                // Check if backend returned tokens in response body
                val accessToken = responseBody?.accessToken
                val refreshToken = responseBody?.refreshToken

                if (accessToken != null && refreshToken != null) {
                    // Save tokens
                    tokenManager.saveTokens(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                    )

                    // Save user info if available
                    responseBody.user?.let {
                        tokenManager.saveUserInfo(
                            userId = it.id,
                            userEmail = it.email,
                        )
                    }

                    // Send FCM token to server after successful verification
                    CoroutineScope(Dispatchers.IO).launch {
                        notificationService.initializeFCMToken()
                    }

                    onResult(true, "Verification successful! Please complete your profile")
                } else {
                    // No tokens in verify response, need to login
                    val loginRequest = com.intern001.dating.data.model.request.LoginRequest(
                        email = email,
                        password = password,
                    )
                    val loginResponse = apiService.login(loginRequest)

                    if (!loginResponse.isSuccessful) {
                        val errorBody = loginResponse.errorBody()?.string()
                        android.util.Log.e("InfoViewModel", "Login failed: $errorBody")
                        onResult(false, "Account created but login failed. Please try logging in manually.")
                        return@launch
                    }

                    val authResponse = loginResponse.body()
                    if (authResponse != null) {
                        // Save tokens from login response
                        tokenManager.saveTokens(
                            accessToken = authResponse.accessToken,
                            refreshToken = authResponse.refreshToken,
                        )

                        // Save user info
                        authResponse.user?.let {
                            tokenManager.saveUserInfo(
                                userId = it.id,
                                userEmail = it.email,
                            )
                        }

                        // Send FCM token to server after successful login
                        CoroutineScope(Dispatchers.IO).launch {
                            notificationService.initializeFCMToken()
                        }

                        onResult(true, "Verification and login successful! Please complete your profile")
                    } else {
                        android.util.Log.e("InfoViewModel", "Login response body is null")
                        onResult(false, "Login failed. Please try again.")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("InfoViewModel", "Error during verify and login", e)
                onResult(false, "Error: ${e.message}")
            }
        }
    }
}
