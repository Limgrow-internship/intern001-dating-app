package com.intern001.dating.domain.model

sealed class LocationPermissionState {
    data object Granted : LocationPermissionState()
    data object Denied : LocationPermissionState()
    data object PermanentlyDenied : LocationPermissionState()
    data object NotRequested : LocationPermissionState()
}

sealed class LocationResult {
    data class Success(val location: UserLocation) : LocationResult()
    data class PermissionDenied(val permissionState: LocationPermissionState) : LocationResult()
    data object LocationDisabled : LocationResult()
    data class Error(val exception: Exception) : LocationResult()
    data object Loading : LocationResult()
}

enum class LocationSource {
    GPS,
    CACHE,
    LAST_KNOWN,
    MANUAL,
    NONE,
}

data class LocationData(
    val location: UserLocation?,
    val source: LocationSource,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float? = null,
)
