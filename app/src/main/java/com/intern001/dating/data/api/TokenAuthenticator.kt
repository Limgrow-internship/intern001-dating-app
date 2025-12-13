package com.intern001.dating.data.api

import android.util.Log
import com.intern001.dating.data.local.prefs.TokenManager
import com.intern001.dating.data.model.response.RefreshTokenRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
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
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(TAG, "🔄 401 detected, attempting to refresh token...")

        // Avoid infinite loop - if we already tried to refresh and still got 401, give up
        val retryCount = response.request.header("Retry-Count")?.toIntOrNull() ?: 0
        if (retryCount >= MAX_RETRY_COUNT) {
            Log.e(TAG, "❌ Max retry count reached, clearing tokens")
            runBlocking {
                tokenManager.clearTokens()
            }
            return null
        }

        // Get refresh token
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.e(TAG, "❌ No refresh token available, user needs to login")
            runBlocking {
                tokenManager.clearTokens()
            }
            return null
        }

        // Synchronize token refresh to avoid multiple simultaneous refresh calls
        return synchronized(this) {
            try {
                Log.d(TAG, "📡 Calling refresh token API...")

                // Call refresh token API (must be blocking since Authenticator is synchronous)
                val refreshResponse = runBlocking {
                    apiService.refreshToken(RefreshTokenRequest(refreshToken = refreshToken))
                }

                if (refreshResponse.isSuccessful) {
                    val newAccessToken = refreshResponse.body()?.accessToken
                    val newRefreshToken = refreshResponse.body()?.refreshToken

                    if (newAccessToken != null && newRefreshToken != null) {
                        Log.d(TAG, "✅ Token refresh successful")

                        // Save new tokens
                        runBlocking {
                            tokenManager.saveTokens(newAccessToken, newRefreshToken)
                        }

                        // Retry original request with new token
                        return response.request.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .header("Retry-Count", (retryCount + 1).toString())
                            .build()
                    } else {
                        Log.e(TAG, "❌ Refresh response missing tokens")
                        runBlocking {
                            tokenManager.clearTokens()
                        }
                        return null
                    }
                } else {
                    val errorBody = refreshResponse.errorBody()?.string()
                    Log.e(TAG, "❌ Token refresh failed: ${refreshResponse.code()} - $errorBody")

                    // Clear tokens if refresh token is also invalid
                    if (refreshResponse.code() == 401) {
                        Log.e(TAG, "❌ Refresh token expired, user needs to login")
                        runBlocking {
                            tokenManager.clearTokens()
                        }
                    }
                    return null
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Token refresh exception", e)
                runBlocking {
                    tokenManager.clearTokens()
                }
                return null
            }
        }
    }
}
