package com.intern001.dating.data.service

import android.content.Context
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "DeviceService"
    }

    /**
     * Get Android Device ID using Settings.Secure.ANDROID_ID
     * This is a unique ID for each device and app installation
     * Returns null if unable to retrieve
     */
    fun getDeviceId(): String? {
        return try {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (androidId.isNullOrEmpty()) {
                Log.w(TAG, "Android ID is null or empty")
                null
            } else {
                Log.d(TAG, "Device ID retrieved: ${androidId.take(8)}...")
                Log.d(TAG, "=== ANDROID DEVICE ID (FULL) ===")
                Log.d(TAG, "Device ID: $androidId")
                Log.d(TAG, "Device ID length: ${androidId.length}")
                Log.d(TAG, "================================")
                androidId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get device ID", e)
            null
        }
    }
}

