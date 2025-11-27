package com.intern001.dating.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "LocationService"
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Lấy vị trí GPS hiện tại
     * @return Location object chứa latitude và longitude, hoặc null nếu không lấy được
     */
    suspend fun getCurrentLocation(): Location? {
        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Location permission not granted")
                return null
            }

            val locationResult = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token,
            ).await()

            if (locationResult != null) {
                Log.d(TAG, "Location retrieved: lat=${locationResult.latitude}, lng=${locationResult.longitude}")
            } else {
                Log.w(TAG, "Location is null")
            }

            locationResult
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location", e)
            null
        }
    }

    /**
     * Lấy latitude và longitude dạng Pair
     * @return Pair<Double, Double>? với first = latitude, second = longitude
     */
    suspend fun getCurrentLocationCoordinates(): Pair<Double, Double>? {
        val location = getCurrentLocation()
        return location?.let { Pair(it.latitude, it.longitude) }
    }
}

