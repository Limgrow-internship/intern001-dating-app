package com.intern001.dating.domain.repository

import com.intern001.dating.domain.model.LocationData
import com.intern001.dating.domain.model.LocationPermissionState
import com.intern001.dating.domain.model.UserLocation

interface LocationRepository {

    suspend fun getLocationData(): LocationData

    suspend fun getUserLocation(): UserLocation?

    fun checkPermissionState(): LocationPermissionState

    fun isLocationEnabled(): Boolean

    fun clearCache()

    fun setManualLocation(latitude: Double, longitude: Double, city: String?, country: String?)
}
