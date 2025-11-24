package com.intern001.dating.domain.usecase.auth

import com.intern001.dating.domain.model.AuthState
import com.intern001.dating.domain.model.common.exception.ValidationException
import com.intern001.dating.domain.model.common.validator.EmailValidator
import com.intern001.dating.domain.model.common.validator.PasswordValidator
import com.intern001.dating.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val emailValidator: EmailValidator,
    private val passwordValidator: PasswordValidator,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        deviceId: String? = null,
    ): Result<AuthState> {
        return try {
            if (!emailValidator.isValid(email)) {
                return Result.failure(ValidationException("Invalid email format"))
            }

            if (!passwordValidator.isValid(password)) {
                return Result.failure(ValidationException("Invalid password format"))
            }

            val loginResult = authRepository.login(email, password, deviceId)
            if (loginResult.isFailure) {
                return Result.failure(loginResult.exceptionOrNull()!!)
            }

            val token = loginResult.getOrNull()!!

            Result.success(
                AuthState(
                    isLoggedIn = true,
                    user = null,
                    token = token,
                    error = null,
                ),
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
