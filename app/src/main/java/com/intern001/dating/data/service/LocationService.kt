package com.intern001.dating.data.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.intern001.dating.domain.model.LocationData
import com.intern001.dating.domain.model.LocationPermissionState
import com.intern001.dating.domain.model.LocationSource
import com.intern001.dating.domain.model.UserLocation
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
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 phút
        private const val MAX_LOCATION_AGE_MS = 30 * 60 * 1000L // 30 phút
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Cache location data
    private var cachedLocationData: LocationData? = null

    /**
     * Kiểm tra trạng thái quyền GPS
     */
    fun checkPermissionState(): LocationPermissionState {
        val hasFineLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return when {
            hasFineLocation || hasCoarseLocation -> LocationPermissionState.Granted
            else -> LocationPermissionState.Denied // Presentation layer sẽ phân biệt Denied vs PermanentlyDenied
        }
    }

    /**
     * Kiểm tra GPS có bật không
     */
    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Lấy location data với chiến lược fallback
     * Priority: GPS -> Cache -> Last Known Location
     */
    suspend fun getLocationData(): LocationData {
        // 1. Kiểm tra quyền trước
        if (checkPermissionState() != LocationPermissionState.Granted) {
            Log.w(TAG, "Location permission not granted")
            return LocationData(
                location = null,
                source = LocationSource.NONE
            )
        }

        // 2. Kiểm tra cache còn hợp lệ không
        cachedLocationData?.let { cached ->
            val age = System.currentTimeMillis() - cached.timestamp
            if (age < CACHE_DURATION_MS) {
                Log.d(TAG, "Using cached location (age: ${age / 1000}s)")
                return cached
            }
        }

        // 3. Thử lấy GPS location mới
        val gpsLocation = getCurrentLocationFromGPS()
        if (gpsLocation != null) {
            val locationData = LocationData(
                location = UserLocation(
                    latitude = gpsLocation.latitude,
                    longitude = gpsLocation.longitude,
                    city = null,
                    country = null
                ),
                source = LocationSource.GPS,
                accuracy = gpsLocation.accuracy,
                timestamp = System.currentTimeMillis()
            )
            cachedLocationData = locationData
            Log.d(TAG, "Got fresh GPS location: ${gpsLocation.latitude}, ${gpsLocation.longitude}")
            return locationData
        }

        // 4. Fallback: Thử last known location
        val lastKnown = getLastKnownLocation()
        if (lastKnown != null) {
            val locationData = LocationData(
                location = UserLocation(
                    latitude = lastKnown.latitude,
                    longitude = lastKnown.longitude,
                    city = null,
                    country = null
                ),
                source = LocationSource.LAST_KNOWN,
                accuracy = lastKnown.accuracy,
                timestamp = lastKnown.time
            )
            cachedLocationData = locationData
            Log.d(TAG, "Using last known location: ${lastKnown.latitude}, ${lastKnown.longitude}")
            return locationData
        }

        // 5. Không có location nào khả dụng
        Log.w(TAG, "No location available from any source")
        return LocationData(
            location = null,
            source = LocationSource.NONE
        )
    }

    /**
     * Lấy vị trí GPS hiện tại
     */
    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocationFromGPS(): Location? {
        return try {
            if (!isLocationEnabled()) {
                Log.w(TAG, "Location services disabled")
                return null
            }

            val locationResult = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token,
            ).await()

            locationResult
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current GPS location", e)
            null
        }
    }

    /**
     * Lấy last known location từ hệ thống
     */
    @SuppressLint("MissingPermission")
    private suspend fun getLastKnownLocation(): Location? {
        return try {
            val lastLocation = fusedLocationClient.lastLocation.await()

            // Chỉ dùng last known location nếu không quá cũ
            if (lastLocation != null) {
                val age = System.currentTimeMillis() - lastLocation.time
                if (age < MAX_LOCATION_AGE_MS) {
                    lastLocation
                } else {
                    Log.w(TAG, "Last known location too old (age: ${age / 1000}s)")
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last known location", e)
            null
        }
    }

    /**
     * Xóa cache location (khi cần force refresh)
     */
    fun clearCache() {
        cachedLocationData = null
        Log.d(TAG, "Location cache cleared")
    }

    /**
     * Lưu manual location (do user nhập thủ công)
     */
    fun setManualLocation(latitude: Double, longitude: Double, city: String?, country: String?) {
        cachedLocationData = LocationData(
            location = UserLocation(
                latitude = latitude,
                longitude = longitude,
                city = city,
                country = country
            ),
            source = LocationSource.MANUAL,
            timestamp = System.currentTimeMillis()
        )
        Log.d(TAG, "Manual location set: $latitude, $longitude")
    }

    // ===== Backward compatibility methods =====

    /**
     * Lấy vị trí GPS hiện tại (deprecated - dùng getLocationData() thay thế)
     * @return Location object chứa latitude và longitude, hoặc null nếu không lấy được
     */
    @Deprecated("Use getLocationData() for better fallback handling")
    suspend fun getCurrentLocation(): Location? {
        val locationData = getLocationData()
        return locationData.location?.let {
            Location("").apply {
                latitude = it.latitude
                longitude = it.longitude
            }
        }
    }

    /**
     * Lấy latitude và longitude dạng Pair (deprecated - dùng getLocationData() thay thế)
     * @return Pair<Double, Double>? với first = latitude, second = longitude
     */
    @Deprecated("Use getLocationData() for better fallback handling")
    suspend fun getCurrentLocationCoordinates(): Pair<Double, Double>? {
        val locationData = getLocationData()
        return locationData.location?.let { Pair(it.latitude, it.longitude) }
    }
}

