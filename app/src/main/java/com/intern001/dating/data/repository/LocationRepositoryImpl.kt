package com.intern001.dating.data.repository

import com.intern001.dating.data.service.LocationService
import com.intern001.dating.domain.model.LocationData
import com.intern001.dating.domain.model.LocationPermissionState
import com.intern001.dating.domain.model.UserLocation
import com.intern001.dating.domain.repository.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Implementation của LocationRepository
 * Delegate tất cả logic cho LocationService
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationService: LocationService,
) : LocationRepository {

    override suspend fun getLocationData(): LocationData {
        return locationService.getLocationData()
    }

    override suspend fun getUserLocation(): UserLocation? {
        val locationData = locationService.getLocationData()
        return locationData.location
    }

    override fun checkPermissionState(): LocationPermissionState {
        return locationService.checkPermissionState()
    }

    override fun isLocationEnabled(): Boolean {
        return locationService.isLocationEnabled()
    }

    override fun clearCache() {
        locationService.clearCache()
    }

    override fun setManualLocation(
        latitude: Double,
        longitude: Double,
        city: String?,
        country: String?,
    ) {
        locationService.setManualLocation(latitude, longitude, city, country)
    }

    override fun observeLocationUpdates(intervalMs: Long): Flow<LocationData> {
        return locationService.observeLocationUpdates(intervalMs)
    }
}
