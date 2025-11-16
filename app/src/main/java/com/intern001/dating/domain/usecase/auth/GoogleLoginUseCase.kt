package com.intern001.dating.domain.usecase.auth

import com.intern001.dating.data.model.response.GoogleLoginResponse
import com.intern001.dating.domain.repository.AuthRepository
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(accessToken: String): Result<GoogleLoginResponse> {
        return repository.googleLogin(accessToken)
    }
}
