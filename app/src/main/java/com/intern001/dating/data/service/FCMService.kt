package com.intern001.dating.data.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.intern001.dating.data.billing.BillingManager
import com.intern001.dating.data.firebase.FirebaseConfigHelper
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FCMService @Inject constructor(
    private val billingManager: BillingManager,
) {
    companion object {
        private const val TAG = "FCMService"
    }

    suspend fun getToken(): String? {
        return try {
            val messaging = if (billingManager.hasActiveSubscription()) {
                FirebaseConfigHelper.getFirebaseMessagingForPremium()
                    ?: FirebaseMessaging.getInstance()
            } else {
                FirebaseMessaging.getInstance()
            }
            messaging?.token?.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }

    suspend fun subscribeToTopic(topic: String) {
        try {
            val messaging = if (billingManager.hasActiveSubscription()) {
                FirebaseConfigHelper.getFirebaseMessagingForPremium()
                    ?: FirebaseMessaging.getInstance()
            } else {
                FirebaseMessaging.getInstance()
            }
            messaging?.subscribeToTopic(topic)?.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to subscribe to topic: $topic", e)
        }
    }

    suspend fun unsubscribeFromTopic(topic: String) {
        try {
            val messaging = if (billingManager.hasActiveSubscription()) {
                FirebaseConfigHelper.getFirebaseMessagingForPremium()
                    ?: FirebaseMessaging.getInstance()
            } else {
                FirebaseMessaging.getInstance()
            }
            messaging?.unsubscribeFromTopic(topic)?.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unsubscribe from topic: $topic", e)
        }
    }
}
