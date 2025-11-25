package com.intern001.dating.data.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FCMService @Inject constructor() {
    companion object {
        private const val TAG = "FCMService"
    }

    /**
     * Get FCM token of the current device
     */
    suspend fun getToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }

    /**
     * Subscribe to receive notifications from a topic
     */
    suspend fun subscribeToTopic(topic: String) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to subscribe to topic: $topic", e)
        }
    }

    /**
     * Unsubscribe from receiving notifications from a topic
     */
    suspend fun unsubscribeFromTopic(topic: String) {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unsubscribe from topic: $topic", e)
        }
    }
}
