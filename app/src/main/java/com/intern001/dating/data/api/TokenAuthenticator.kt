package com.intern001.dating.data.api

import android.util.Log
import com.intern001.dating.data.local.prefs.TokenManager
import com.intern001.dating.data.model.response.RefreshTokenRequest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * TokenAuthenticator automatically refreshes the access token when a 401 response is received.
 *
 * Flow:
 * 1. Detect 401 Unauthorized response
 * 2. Call /api/auth/refresh with refresh token
 * 3. Save new access token
 * 4. Retry original request with new token
 * 5. If refresh fails → clear tokens (user needs to login again)
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: DatingApiService,
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
        private const val MAX_RETRY_COUNT = 3
        private const val REFRESH_TIMEOUT_SECONDS = 10L
    }

    private val refreshScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(TAG, "🔄 401 detected, attempting to refresh token...")

        // Avoid infinite loop - if we already tried to refresh and still got 401, give up
        val retryCount = response.request.header("Retry-Count")?.toIntOrNull() ?: 0
        if (retryCount >= MAX_RETRY_COUNT) {
            Log.e(TAG, "❌ Max retry count reached, clearing tokens")
            clearTokensSync()
            return null
        }

        // Get refresh token
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.e(TAG, "❌ No refresh token available, user needs to login")
            clearTokensSync()
            return null
        }

        // Synchronize token refresh to avoid multiple simultaneous refresh calls
        return synchronized(this) {
            try {
                Log.d(TAG, "📡 Calling refresh token API...")

                // Use CountDownLatch to wait for async operation with timeout
                val latch = CountDownLatch(1)
                var refreshResponse: retrofit2.Response<com.intern001.dating.data.model.response.AuthResponse>? = null
                var exception: Exception? = null

                // Call refresh token API asynchronously
                refreshScope.launch {
                    try {
                        refreshResponse = apiService.refreshToken(RefreshTokenRequest(refreshToken = refreshToken))
                    } catch (e: Exception) {
                        exception = e
                    } finally {
                        latch.countDown()
                    }
                }

                // Wait for response with timeout
                val completed = latch.await(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                if (!completed) {
                    Log.e(TAG, "❌ Token refresh timeout")
                    clearTokensSync()
                    return null
                }

                if (exception != null) {
                    Log.e(TAG, "❌ Token refresh exception", exception)
                    clearTokensSync()
                    return null
                }

                val refreshApiResponse = refreshResponse ?: run {
                    Log.e(TAG, "❌ Token refresh response is null")
                    clearTokensSync()
                    return null
                }

                if (refreshApiResponse.isSuccessful) {
                    val newAccessToken = refreshApiResponse.body()?.accessToken
                    val newRefreshToken = refreshApiResponse.body()?.refreshToken

                    if (newAccessToken != null && newRefreshToken != null) {
                        Log.d(TAG, "✅ Token refresh successful")

                        // Save new tokens synchronously (SharedPreferences.apply() is fast)
                        tokenManager.saveTokens(newAccessToken, newRefreshToken)

                        // Retry original request with new token (use original response parameter)
                        return response.request.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .header("Retry-Count", (retryCount + 1).toString())
                            .build()
                    } else {
                        Log.e(TAG, "❌ Refresh response missing tokens")
                        clearTokensSync()
                        return null
                    }
                } else {
                    val errorBody = refreshApiResponse.errorBody()?.string()
                    Log.e(TAG, "❌ Token refresh failed: ${refreshApiResponse.code()} - $errorBody")

                    // Clear tokens if refresh token is also invalid
                    if (refreshApiResponse.code() == 401) {
                        Log.e(TAG, "❌ Refresh token expired, user needs to login")
                        clearTokensSync()
                    }
                    return null
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Token refresh exception", e)
                clearTokensSync()
                return null
            }
        }
    }

    private fun clearTokensSync() {
        // SharedPreferences.apply() is asynchronous but fast enough for this use case
        // If we need synchronous, we can use commit() but it's blocking
        tokenManager.clearTokens()
    }
}
