package com.intern001.dating.domain.model.common.validator

class PasswordValidator {
    fun isValid(password: String): Boolean {
        return password.length >= 8 &&
            password.any { it.isLetter() } &&
            password.any { it.isDigit() }
    }
}
