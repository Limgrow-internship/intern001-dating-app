package com.intern001.dating.domain.model

data class VerificationResult(
    val success: Boolean,
    val message: String? = null,
)
