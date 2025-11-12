package com.intern001.dating.presentation.util

import com.google.android.material.textfield.TextInputLayout

object ValidationHelper {

    fun validateEmail(
        email: String,
        tilEmail: TextInputLayout,
    ): Boolean {
        return when {
            email.isEmpty() -> {
                tilEmail.error = "Email is required"
                false
            }
            !isValidEmailFormat(email) -> {
                tilEmail.error = "Invalid email format"
                false
            }
            else -> {
                tilEmail.error = null
                true
            }
        }
    }

    fun validatePassword(
        password: String,
        tilPassword: TextInputLayout,
        minLength: Int = 6,
    ): Boolean {
        return when {
            password.isEmpty() -> {
                tilPassword.error = "Password is required"
                false
            }
            password.length < minLength -> {
                tilPassword.error = "Password must be at least $minLength characters"
                false
            }
            else -> {
                tilPassword.error = null
                true
            }
        }
    }

    fun validateConfirmPassword(
        password: String,
        confirmPassword: String,
        tilConfirmPassword: TextInputLayout,
    ): Boolean {
        return when {
            confirmPassword.isEmpty() -> {
                tilConfirmPassword.error = "Confirm password is required"
                false
            }
            password != confirmPassword -> {
                tilConfirmPassword.error = "Passwords do not match"
                false
            }
            else -> {
                tilConfirmPassword.error = null
                true
            }
        }
    }

    fun validateNotEmpty(
        value: String,
        textInputLayout: TextInputLayout,
        fieldName: String,
    ): Boolean {
        return if (value.isEmpty()) {
            textInputLayout.error = "$fieldName is required"
            false
        } else {
            textInputLayout.error = null
            true
        }
    }

    fun clearError(textInputLayout: TextInputLayout) {
        textInputLayout.error = null
    }

    private fun isValidEmailFormat(email: String): Boolean {
        return email.matches(
            Regex(
                "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Z|a-z]{2,})$",
            ),
        )
    }
}
