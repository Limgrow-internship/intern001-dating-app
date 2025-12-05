package com.intern001.dating.data.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.intern001.dating.R
import java.io.InputStream
import org.json.JSONObject

object FirebaseConfigHelper {
    private const val PREMIUM_FIREBASE_APP_NAME = "premium_firebase"

    fun initializeFirebaseForPremium(context: Context): Boolean {
        return try {
            try {
                FirebaseApp.getInstance(PREMIUM_FIREBASE_APP_NAME)
                return true
            } catch (e: IllegalStateException) {
            }

            val inputStream: InputStream = context.resources.openRawResource(R.raw.google_services_premium)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()

            val jsonObject = JSONObject(jsonString)
            val projectInfo = jsonObject.getJSONObject("project_info")
            val client = jsonObject.getJSONArray("client").getJSONObject(0)
            val clientInfo = client.getJSONObject("client_info")
            val apiKey = client.getJSONArray("api_key").getJSONObject(0)

            val options = FirebaseOptions.Builder()
                .setProjectId(projectInfo.getString("project_id"))
                .setApplicationId(clientInfo.getString("mobilesdk_app_id"))
                .setApiKey(apiKey.getString("current_key"))
                .setGcmSenderId(projectInfo.getString("project_number"))
                .build()

            FirebaseApp.initializeApp(context, options, PREMIUM_FIREBASE_APP_NAME)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getFirebaseMessagingForPremium(): FirebaseMessaging? {
        return try {
            val premiumApp = FirebaseApp.getInstance(PREMIUM_FIREBASE_APP_NAME)
            val method = FirebaseMessaging::class.java.getDeclaredMethod("getInstance", FirebaseApp::class.java)
            method.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            method.invoke(null, premiumApp) as FirebaseMessaging
        } catch (e: NoSuchMethodException) {
            FirebaseMessaging.getInstance()
        } catch (e: Exception) {
            FirebaseMessaging.getInstance()
        }
    }

    fun isPremiumFirebaseInitialized(): Boolean {
        return try {
            FirebaseApp.getInstance(PREMIUM_FIREBASE_APP_NAME)
            true
        } catch (e: Exception) {
            false
        }
    }
}
