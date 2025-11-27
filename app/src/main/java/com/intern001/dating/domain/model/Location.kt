package com.intern001.dating.domain.model

/**
 * Trạng thái quyền GPS của người dùng
 */
sealed class LocationPermissionState {
    /** Quyền đã được cấp và có thể lấy location */
    data object Granted : LocationPermissionState()

    /** Quyền bị từ chối */
    data object Denied : LocationPermissionState()

    /** Quyền bị từ chối vĩnh viễn (user chọn "Don't ask again") */
    data object PermanentlyDenied : LocationPermissionState()

    /** Chưa request quyền */
    data object NotRequested : LocationPermissionState()
}

/**
 * Kết quả khi lấy location
 */
sealed class LocationResult {
    /** Lấy location thành công */
    data class Success(val location: UserLocation) : LocationResult()

    /** Không có quyền GPS */
    data class PermissionDenied(val permissionState: LocationPermissionState) : LocationResult()

    /** GPS tắt hoặc không khả dụng */
    data object LocationDisabled : LocationResult()

    /** Lỗi khi lấy location */
    data class Error(val exception: Exception) : LocationResult()

    /** Đang lấy location */
    data object Loading : LocationResult()
}

/**
 * Nguồn dữ liệu location
 */
enum class LocationSource {
    /** GPS chính xác cao */
    GPS,

    /** Location cache còn hợp lệ */
    CACHE,

    /** Last known location từ hệ thống */
    LAST_KNOWN,

    /** Location do user nhập thủ công */
    MANUAL,

    /** Không có location */
    NONE
}

/**
 * Location data với metadata
 */
data class LocationData(
    val location: UserLocation?,
    val source: LocationSource,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float? = null, // độ chính xác tính bằng mét
)
