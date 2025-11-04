package com.example.heartondatingapp.domain.model.common.validator

class EmailValidator {
    fun isValid(email: String): Boolean {
        return email.matches(
            Regex(
                "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Z|a-z]{2,})$",
            ),
        )
    }
}
