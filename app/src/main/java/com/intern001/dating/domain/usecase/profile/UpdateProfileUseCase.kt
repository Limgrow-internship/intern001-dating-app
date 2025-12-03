package com.intern001.dating.domain.usecase.profile

import com.intern001.dating.data.model.request.LocationRequest
import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.domain.model.UpdateProfile
import com.intern001.dating.domain.repository.AuthRepository
import com.intern001.dating.domain.repository.LocationRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(request: UpdateProfileRequest): Result<UpdateProfile> {
        return try {
            val enrichedRequest = attachLocationIfAvailable(request)
            val result = authRepository.updateUserProfile(enrichedRequest)
            result.map { it as UpdateProfile }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun attachLocationIfAvailable(request: UpdateProfileRequest): UpdateProfileRequest {
        if (request.location != null) return request

        val latestLocation = runCatching { locationRepository.getUserLocation() }.getOrNull()
        val latitude = latestLocation?.latitude
        val longitude = latestLocation?.longitude

        if (latitude == null || longitude == null) {
            return request
        }

        val locationRequest =
            LocationRequest(
                type = "Point",
                coordinates = listOf(longitude, latitude),
                latitude = latitude,
                longitude = longitude,
                city = request.city ?: latestLocation.city,
                country = request.country ?: latestLocation.country,
            )

        return request.copy(
            location = locationRequest,
            city = request.city ?: latestLocation.city,
            country = request.country ?: latestLocation.country,
        )
    }
}
