package com.example.heartondatingapp.domain.usecase.auth

import android.annotation.SuppressLint
import com.example.heartondatingapp.domain.model.AuthState
import com.example.heartondatingapp.domain.model.common.exception.ValidationException
import com.example.heartondatingapp.domain.model.common.validator.EmailValidator
import com.example.heartondatingapp.domain.model.common.validator.PasswordValidator
import com.example.heartondatingapp.domain.repository.AuthRepository
import java.text.SimpleDateFormat
import java.util.Date

class SignupUseCase(
    private val authRepository: AuthRepository,
    private val emailValidator: EmailValidator,
    private val passwordValidator: PasswordValidator,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String,
        gender: String,
        dateOfBirth: String,
        deviceToken: String? = null,
    ): Result<AuthState> {
        return try {
            validateInputs(email, password, confirmPassword, firstName, lastName, gender, dateOfBirth)

            authRepository.signup(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                gender = gender,
                dateOfBirth = dateOfBirth,
                deviceToken = deviceToken,
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validateInputs(
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String,
        gender: String,
        dateOfBirth: String,
    ) {
        when {
            !emailValidator.isValid(email) ->
                throw ValidationException("Invalid email")

            !passwordValidator.isValid(password) ->
                throw ValidationException("Password must be at least 8 characters, including at least 1 letter and 1 number")

            password != confirmPassword ->
                throw ValidationException("Passwords do not match")

            firstName.isBlank() || firstName.length > 30 ->
                throw ValidationException("Name must be 2-30 characters")

            lastName.isBlank() || lastName.length > 30 ->
                throw ValidationException("Last name must be 2-30 characters")

            gender.isEmpty() ->
                throw ValidationException("Please select gender")

            !isValidAge(dateOfBirth) ->
                throw ValidationException("You must be over 18 years old")
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun isValidAge(dateOfBirth: String): Boolean {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy")
            val dob = format.parse(dateOfBirth) ?: return false
            val today = Date()
            val age = (today.time - dob.time) / (365.25 * 24 * 60 * 60 * 1000)
            age >= 18
        } catch (e: Exception) {
            false
        }
    }
}
