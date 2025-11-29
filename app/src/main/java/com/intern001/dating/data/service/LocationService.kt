package com.intern001.dating.data.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.intern001.dating.domain.model.LocationData
import com.intern001.dating.domain.model.LocationPermissionState
import com.intern001.dating.domain.model.LocationSource
import com.intern001.dating.domain.model.UserLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.tasks.await

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "LocationService"
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L
        private const val MAX_LOCATION_AGE_MS = 30 * 60 * 1000L
        private const val MIN_UPDATE_DISTANCE_METERS = 10f
        private const val REALTIME_LOCATION_INTERVAL_MS = 30_000L
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Cache location data
    private var cachedLocationData: LocationData? = null

    fun checkPermissionState(): LocationPermissionState {
        val hasFineLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return when {
            hasFineLocation || hasCoarseLocation -> LocationPermissionState.Granted
            else -> LocationPermissionState.Denied
        }
    }

    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    suspend fun getLocationData(): LocationData {
        if (checkPermissionState() != LocationPermissionState.Granted) {
            Log.w(TAG, "Location permission not granted")
            return LocationData(
                location = null,
                source = LocationSource.NONE,
            )
        }

        cachedLocationData?.let { cached ->
            val age = System.currentTimeMillis() - cached.timestamp
            if (age < CACHE_DURATION_MS) {
                Log.d(TAG, "Using cached location (age: ${age / 1000}s)")
                return cached
            }
        }

        val gpsLocation = getCurrentLocationFromGPS()
        if (gpsLocation != null) {
            val locationData = LocationData(
                location = UserLocation(
                    latitude = gpsLocation.latitude,
                    longitude = gpsLocation.longitude,
                    city = null,
                    country = null,
                ),
                source = LocationSource.GPS,
                accuracy = gpsLocation.accuracy,
                timestamp = System.currentTimeMillis(),
            )
            cachedLocationData = locationData
            Log.d(TAG, "Got fresh GPS location: ${gpsLocation.latitude}, ${gpsLocation.longitude}")
            return locationData
        }

        val lastKnown = getLastKnownLocation()
        if (lastKnown != null) {
            val locationData = LocationData(
                location = UserLocation(
                    latitude = lastKnown.latitude,
                    longitude = lastKnown.longitude,
                    city = null,
                    country = null,
                ),
                source = LocationSource.LAST_KNOWN,
                accuracy = lastKnown.accuracy,
                timestamp = lastKnown.time,
            )
            cachedLocationData = locationData
            Log.d(TAG, "Using last known location: ${lastKnown.latitude}, ${lastKnown.longitude}")
            return locationData
        }

        Log.w(TAG, "No location available from any source")
        return LocationData(
            location = null,
            source = LocationSource.NONE,
        )
    }

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

    @SuppressLint("MissingPermission")
    private suspend fun getLastKnownLocation(): Location? {
        return try {
            val lastLocation = fusedLocationClient.lastLocation.await()

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

    fun clearCache() {
        cachedLocationData = null
        Log.d(TAG, "Location cache cleared")
    }

    fun setManualLocation(latitude: Double, longitude: Double, city: String?, country: String?) {
        cachedLocationData = LocationData(
            location = UserLocation(
                latitude = latitude,
                longitude = longitude,
                city = city,
                country = country,
            ),
            source = LocationSource.MANUAL,
            timestamp = System.currentTimeMillis(),
        )
        Log.d(TAG, "Manual location set: $latitude, $longitude")
    }

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

    @Deprecated("Use getLocationData() for better fallback handling")
    suspend fun getCurrentLocationCoordinates(): Pair<Double, Double>? {
        val locationData = getLocationData()
        return locationData.location?.let { Pair(it.latitude, it.longitude) }
    }

    fun observeLocationUpdates(intervalMs: Long = REALTIME_LOCATION_INTERVAL_MS): Flow<LocationData> = callbackFlow {
        if (checkPermissionState() != LocationPermissionState.Granted) {
            trySend(LocationData(location = null, source = LocationSource.NONE))
            close()
            return@callbackFlow
        }

        if (!isLocationEnabled()) {
            trySend(LocationData(location = null, source = LocationSource.NONE))
            close()
            return@callbackFlow
        }

        cachedLocationData?.let { cached ->
            trySend(cached)
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, intervalMs)
            .setMinUpdateDistanceMeters(MIN_UPDATE_DISTANCE_METERS)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val lastLocation = result.lastLocation ?: return
                val locationData = lastLocation.toLocationData()
                cachedLocationData = locationData
                trySend(locationData).isSuccess
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                callback,
                Looper.getMainLooper(),
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing location permission for updates", e)
            trySend(LocationData(location = null, source = LocationSource.NONE))
            close(e)
            return@callbackFlow
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request location updates", e)
            trySend(LocationData(location = null, source = LocationSource.NONE))
            close(e)
            return@callbackFlow
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }.conflate()

    private fun Location.toLocationData(): LocationData {
        val source = when (provider) {
            LocationManager.GPS_PROVIDER -> LocationSource.GPS
            else -> LocationSource.LAST_KNOWN
        }
        return LocationData(
            location = UserLocation(
                latitude = latitude,
                longitude = longitude,
                city = null,
                country = null,
            ),
            source = source,
            accuracy = accuracy,
            timestamp = if (time > 0) time else System.currentTimeMillis(),
        )
    }
}
