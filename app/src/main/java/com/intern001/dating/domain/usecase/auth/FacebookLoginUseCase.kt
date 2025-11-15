package com.intern001.dating.domain.usecase.auth

import com.intern001.dating.data.model.response.FacebookLoginResponse
import com.intern001.dating.domain.repository.AuthRepository

class FacebookLoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(accessToken: String): Result<FacebookLoginResponse> {
        return repository.facebookLogin(accessToken)
    }
}
