package com.intern001.dating.domain.usecase

import com.intern001.dating.domain.model.LocationData
import com.intern001.dating.domain.model.LocationPermissionState
import com.intern001.dating.domain.model.LocationResult
import com.intern001.dating.domain.model.LocationSource
import com.intern001.dating.domain.repository.LocationRepository
import javax.inject.Inject

class GetLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(): LocationResult {
        val permissionState = locationRepository.checkPermissionState()
        if (permissionState != LocationPermissionState.Granted) {
            return LocationResult.PermissionDenied(permissionState)
        }

        if (!locationRepository.isLocationEnabled()) {
            return LocationResult.LocationDisabled
        }

        return try {
            val locationData = locationRepository.getLocationData()

            if (locationData.location != null) {
                LocationResult.Success(locationData.location)
            } else {
                LocationResult.PermissionDenied(permissionState)
            }
        } catch (e: Exception) {
            LocationResult.Error(e)
        }
    }

    suspend fun getLocationData(): LocationData {
        return locationRepository.getLocationData()
    }
}

class ShouldShowLocationWarningUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(): Boolean {
        val locationData = locationRepository.getLocationData()

        return when {
            locationRepository.checkPermissionState() != LocationPermissionState.Granted -> true
            locationData.location == null -> true
            locationData.source == LocationSource.NONE -> true
            locationData.source == LocationSource.LAST_KNOWN -> {
                val age = System.currentTimeMillis() - locationData.timestamp
                age > 10 * 60 * 1000L
            }
            locationData.source == LocationSource.MANUAL -> true
            else -> false
        }
    }

    suspend fun getWarningMessage(): String? {
        val locationData = locationRepository.getLocationData()

        return when {
            locationRepository.checkPermissionState() != LocationPermissionState.Granted -> {
                "Bật quyền GPS để tìm người gần bạn"
            }
            !locationRepository.isLocationEnabled() -> {
                "Bật GPS để tìm người gần bạn"
            }
            locationData.source == LocationSource.LAST_KNOWN -> {
                "Đang dùng vị trí cũ. Bật GPS để cập nhật"
            }
            locationData.source == LocationSource.MANUAL -> {
                "Đang dùng vị trí thủ công"
            }
            locationData.location == null -> {
                "Không lấy được vị trí. Chất lượng gợi ý có thể bị hạn chế"
            }
            else -> null
        }
    }
}

class RefreshLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(): LocationData {
        locationRepository.clearCache()
        return locationRepository.getLocationData()
    }
}
