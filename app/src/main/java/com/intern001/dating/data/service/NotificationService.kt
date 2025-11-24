package com.intern001.dating.data.service

import android.util.Log
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.request.UpdateFCMTokenRequest
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class NotificationService @Inject constructor(
    @Named("datingApi") private val apiService: DatingApiService,
    private val tokenManager: TokenManager,
    private val fcmService: FCMService,
) {
    companion object {
        private const val TAG = "NotificationService"
    }

    /**
     * Send FCM token to server so server can send notifications
     */
    suspend fun sendTokenToServer(token: String): Result<Unit> {
        return try {
            val response = apiService.updateFCMToken(UpdateFCMTokenRequest(fcmToken = token))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to send FCM token: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to send FCM token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception sending FCM token", e)
            Result.failure(e)
        }
    }

    /**
     * Called when FCM token is refreshed
     */
    fun onTokenRefresh(newToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            sendTokenToServer(newToken)
        }
    }

    /**
     * Initialize and send FCM token to server
     */
    suspend fun initializeFCMToken(): Result<String?> {
        return try {
            val token = fcmService.getToken()
            if (token != null) {
                sendTokenToServer(token).fold(
                    onSuccess = {
                        Result.success(token)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to send FCM token to server", error)
                        Result.success(token) // Still return token even if sending to server failed
                    },
                )
            } else {
                Log.e(TAG, "Failed to get FCM token")
                Result.failure(Exception("Failed to get FCM token"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception initializing FCM token", e)
            Result.failure(e)
        }
    }
}
