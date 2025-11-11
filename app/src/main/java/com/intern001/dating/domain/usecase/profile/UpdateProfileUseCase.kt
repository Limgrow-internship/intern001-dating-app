package com.intern001.dating.domain.usecase.profile

import com.intern001.dating.domain.model.User
import com.intern001.dating.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        firstName: String? = null,
        lastName: String? = null,
        dateOfBirth: String? = null,
        gender: String? = null,
        profileImageUrl: String? = null,
        goal: String? = null,
        bio: String? = null,
    ): Result<User> {
        return authRepository.updateUserProfile(
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            gender = gender,
            profileImageUrl = profileImageUrl,
            goal = goal,
            bio = bio,
        )
    }
}
