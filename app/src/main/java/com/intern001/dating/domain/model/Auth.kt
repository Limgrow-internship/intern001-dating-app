package com.intern001.dating.domain.model

import java.util.Date

data class User(
    val id: String,
    val email: String,
    val phoneNumber: String? = null,
    val isVerified: Boolean = false,
    val status: String = "active",
    val lastLogin: Date? = null,
    val createdAt: Date,
    val updatedAt: Date,
)

data class UserProfile(
    val id: String,
    val userId: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val displayName: String? = null,
    val dateOfBirth: Date? = null,
    val avatar: String? = null,
    val bio: String? = null,
    val age: Int? = null,
    val gender: String? = null, // 'male' | 'female' | 'other'
    val interests: List<String> = emptyList(),
    val mode: String? = null, // 'dating' | 'friend'
    val relationshipMode: String? = null, // 'serious' | 'casual' | 'friendship'
    val height: Int? = null, // in centimeters (120-220)
    val weight: Int? = null, // in kilograms (30-300)
    val location: UserLocation? = null,
    val city: String? = null,
    val country: String? = null,
    val occupation: String? = null,
    val company: String? = null,
    val education: String? = null,
    val zodiacSign: String? = null,
    val verifiedAt: Date? = null,
    val verifiedBadge: Boolean = false,
    val isVerified: Boolean = false,
    val photos: List<Photo> = emptyList(),
    val profileCompleteness: Int = 0, // 0-100
    val profileViews: Int = 0,
    val goals: List<String> = emptyList(),
    val job: String? = null,
    val openQuestionAnswers: Map<String, String>? = null,
    val createdAt: Date,
    val updatedAt: Date,
)

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String? = null,
    val country: String? = null,
)

data class Photo(
    val id: String,
    val userId: String? = null, // Optional in domain model (usually from profile context)
    val url: String,
    val cloudinaryPublicId: String? = null,
    val type: String, // 'avatar' | 'gallery' | 'selfie'
    val source: String, // 'upload' | 'google' | 'facebook' | 'apple'
    val isPrimary: Boolean = false,
    val order: Int = 0,
    val isVerified: Boolean = false,
    val width: Int? = null,
    val height: Int? = null,
    val fileSize: Int? = null, // in bytes
    val format: String? = null, // 'jpg', 'png', etc.
    val isActive: Boolean = true,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
)

data class UserPreferences(
    val id: String,
    val userId: String,
    val seekingGender: List<String> = emptyList(),
    val ageRange: Range? = null,
    val heightRange: Range? = null,
    val distanceRange: Range? = null,
    val interests: List<String> = emptyList(),
    val relationshipModes: List<String> = emptyList(),
    val hasChildren: String? = null,
    val wantChildren: String? = null,
    val smokingStatus: String? = null,
    val drinkingStatus: String? = null,
    val educationLevel: List<String> = emptyList(),
    val updatedAt: Date,
)

data class Range(
    val min: Int,
    val max: Int,
)

data class AuthState(
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val token: String? = null,
    val error: String? = null,
)

data class MatchStatusGet(
    val matched: Boolean,
    val userLiked: Boolean,
    val targetLiked: Boolean,
    val targetProfile: TargetProfile?
)

data class TargetProfile(
    val displayName: String?,
    val age: Int?,
    val gender: String?,
    val bio: String?,
    val interests: List<String>,
    val city: String?,
    val occupation: String?,
    val height: Int?
)

// Type alias for backward compatibility
// UpdateProfile is the same as UserProfile
typealias UpdateProfile = UserProfile
