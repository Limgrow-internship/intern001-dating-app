package com.intern001.dating.domain.usecase

import com.intern001.dating.domain.model.LocationData
import com.intern001.dating.domain.model.LocationPermissionState
import com.intern001.dating.domain.model.LocationResult
import com.intern001.dating.domain.model.LocationSource
import com.intern001.dating.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Use case để lấy location với đầy đủ metadata và error handling
 */
class GetLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    /**
     * Lấy location với xử lý đầy đủ các trường hợp
     * @return LocationResult chứa thông tin chi tiết về kết quả
     */
    suspend operator fun invoke(): LocationResult {
        // 1. Kiểm tra quyền
        val permissionState = locationRepository.checkPermissionState()
        if (permissionState != LocationPermissionState.Granted) {
            return LocationResult.PermissionDenied(permissionState)
        }

        // 2. Kiểm tra GPS có bật không
        if (!locationRepository.isLocationEnabled()) {
            return LocationResult.LocationDisabled
        }

        // 3. Lấy location data
        return try {
            val locationData = locationRepository.getLocationData()

            if (locationData.location != null) {
                LocationResult.Success(locationData.location)
            } else {
                // Không có location khả dụng
                LocationResult.PermissionDenied(permissionState)
            }
        } catch (e: Exception) {
            LocationResult.Error(e)
        }
    }

    /**
     * Lấy LocationData với metadata đầy đủ (source, timestamp, accuracy)
     */
    suspend fun getLocationData(): LocationData {
        return locationRepository.getLocationData()
    }
}

/**
 * Use case để kiểm tra có nên hiển thị cảnh báo location không
 */
class ShouldShowLocationWarningUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    /**
     * @return true nếu cần hiển thị cảnh báo "Không có GPS"
     */
    suspend operator fun invoke(): Boolean {
        val locationData = locationRepository.getLocationData()

        // Hiển thị cảnh báo nếu:
        // 1. Không có quyền GPS
        // 2. Không có location nào khả dụng
        // 3. Đang dùng location cũ (LAST_KNOWN) hoặc manual
        return when {
            locationRepository.checkPermissionState() != LocationPermissionState.Granted -> true
            locationData.location == null -> true
            locationData.source == LocationSource.NONE -> true
            locationData.source == LocationSource.LAST_KNOWN -> {
                // Cảnh báo nếu last known location quá cũ (> 10 phút)
                val age = System.currentTimeMillis() - locationData.timestamp
                age > 10 * 60 * 1000L
            }
            locationData.source == LocationSource.MANUAL -> true
            else -> false
        }
    }

    /**
     * Lấy message cảnh báo phù hợp
     */
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

/**
 * Use case để refresh location (xóa cache và lấy mới)
 */
class RefreshLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(): LocationData {
        locationRepository.clearCache()
        return locationRepository.getLocationData()
    }
}
