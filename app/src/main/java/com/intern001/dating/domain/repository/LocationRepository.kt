package com.intern001.dating.domain.repository

import com.intern001.dating.domain.model.LocationData
import com.intern001.dating.domain.model.LocationPermissionState
import com.intern001.dating.domain.model.UserLocation

/**
 * Repository interface cho location operations
 */
interface LocationRepository {
    /**
     * Lấy location data với chiến lược fallback
     * Priority: GPS -> Cache -> Last Known Location
     */
    suspend fun getLocationData(): LocationData

    /**
     * Lấy user location (nullable) cho backward compatibility
     */
    suspend fun getUserLocation(): UserLocation?

    /**
     * Kiểm tra trạng thái quyền GPS
     */
    fun checkPermissionState(): LocationPermissionState

    /**
     * Kiểm tra GPS có bật không
     */
    fun isLocationEnabled(): Boolean

    /**
     * Xóa cache location (khi cần force refresh)
     */
    fun clearCache()

    /**
     * Lưu manual location (do user nhập thủ công)
     */
    fun setManualLocation(latitude: Double, longitude: Double, city: String?, country: String?)
}
