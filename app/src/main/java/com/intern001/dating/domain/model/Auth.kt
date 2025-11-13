package com.intern001.dating.domain.model

import java.util.Date

data class User(
    val id: String,
    val email: String,
    val phoneNumber: String? = null,
    val firstName: String,
    val lastName: String,
    val displayName: String,
    val avatar: String? = null,
    val bio: String? = null,
    val age: Int? = null,
    val gender: String?,
    val interests: List<String> = emptyList(),
    val relationshipMode: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val location: UserLocation? = null,
    val occupation: String? = null,
    val company: String? = null,
    val education: String? = null,
    val zodiacSign: String? = null,
    val isVerified: Boolean = false,
    val photos: List<Photo> = emptyList(),
    val profileCompleteness: Int = 0,
    val profileViews: Int = 0,
    val lastLogin: Date? = null,
    val status: String = "active",
    val createdAt: Date,
    val updatedAt: Date,
)

data class UserProfile(
    val id: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val displayName: String,
    val avatar: String? = null,
    val bio: String? = null,
    val age: Int? = null,
    val gender: String?,
    val interests: List<String> = emptyList(),
    val relationshipMode: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val location: UserLocation? = null,
    val occupation: String? = null,
    val company: String? = null,
    val education: String? = null,
    val zodiacSign: String? = null,
    val isVerified: Boolean = false,
    val photos: List<Photo> = emptyList(),
    val profileCompleteness: Int = 0,
    val profileViews: Int = 0,
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
    val url: String,
    val order: Int = 0,
    val uploadedAt: Date? = null,
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
