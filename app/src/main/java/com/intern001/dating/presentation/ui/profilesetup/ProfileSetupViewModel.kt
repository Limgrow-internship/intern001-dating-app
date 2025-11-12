package com.intern001.dating.presentation.ui.profilesetup

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.domain.usecase.profile.UpdateProfileUseCase
import com.intern001.dating.domain.usecase.profile.UploadImageUseCase
import com.intern001.dating.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadImageUseCase: UploadImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    // Profile data
    var photo1Url: String? = null
    var photo2Url: String? = null
    var photo3Url: String? = null
    var name: String = ""
    var gender: String = ""
    var dateOfBirth: String = ""
    var mode: String = ""

    fun updateProfile() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                android.util.Log.d("ProfileSetupViewModel", "Starting profile update...")
                android.util.Log.d("ProfileSetupViewModel", "Name: $name")
                android.util.Log.d("ProfileSetupViewModel", "Gender: $gender")
                android.util.Log.d("ProfileSetupViewModel", "Date of Birth: $dateOfBirth")
                android.util.Log.d("ProfileSetupViewModel", "Mode: $mode")

                // Validation
                if (name.isEmpty()) {
                    _uiState.value = UiState.Error("Name is required")
                    return@launch
                }
                if (gender.isEmpty()) {
                    _uiState.value = UiState.Error("Gender is required")
                    return@launch
                }
                if (dateOfBirth.isEmpty()) {
                    _uiState.value = UiState.Error("Date of birth is required")
                    return@launch
                }
                if (mode.isEmpty()) {
                    _uiState.value = UiState.Error("Mode is required")
                    return@launch
                }

                // Upload images to Cloudinary if available
                var uploadedPhoto1Url: String? = null
                var uploadedPhoto2Url: String? = null
                var uploadedPhoto3Url: String? = null

                // Only upload if photos are selected
                if (photo1Url != null || photo2Url != null || photo3Url != null) {
                    android.util.Log.d("ProfileSetupViewModel", "Uploading photos to Cloudinary...")

                    photo1Url?.let { uriString ->
                        android.util.Log.d("ProfileSetupViewModel", "Uploading photo 1: $uriString")
                        val result = uploadImageUseCase(uriString.toUri())
                        result.fold(
                            onSuccess = { url ->
                                uploadedPhoto1Url = url
                                android.util.Log.d("ProfileSetupViewModel", "Photo 1 uploaded: $url")
                            },
                            onFailure = { error ->
                                android.util.Log.e("ProfileSetupViewModel", "Failed to upload photo 1", error)
                                _uiState.value = UiState.Error("Failed to upload photo 1: ${error.message}")
                                return@launch
                            }
                        )
                    }

                    photo2Url?.let { uriString ->
                        android.util.Log.d("ProfileSetupViewModel", "Uploading photo 2: $uriString")
                        val result = uploadImageUseCase(uriString.toUri())
                        result.fold(
                            onSuccess = { url ->
                                uploadedPhoto2Url = url
                                android.util.Log.d("ProfileSetupViewModel", "Photo 2 uploaded: $url")
                            },
                            onFailure = { error ->
                                android.util.Log.e("ProfileSetupViewModel", "Failed to upload photo 2", error)
                                _uiState.value = UiState.Error("Failed to upload photo 2: ${error.message}")
                                return@launch
                            }
                        )
                    }

                    photo3Url?.let { uriString ->
                        android.util.Log.d("ProfileSetupViewModel", "Uploading photo 3: $uriString")
                        val result = uploadImageUseCase(uriString.toUri())
                        result.fold(
                            onSuccess = { url ->
                                uploadedPhoto3Url = url
                                android.util.Log.d("ProfileSetupViewModel", "Photo 3 uploaded: $url")
                            },
                            onFailure = { error ->
                                android.util.Log.e("ProfileSetupViewModel", "Failed to upload photo 3", error)
                                _uiState.value = UiState.Error("Failed to upload photo 3: ${error.message}")
                                return@launch
                            }
                        )
                    }
                } else {
                    android.util.Log.d("ProfileSetupViewModel", "No photos selected, skipping upload")
                }

                // Use the first uploaded photo as profile image
                val profileImage = uploadedPhoto1Url ?: uploadedPhoto2Url ?: uploadedPhoto3Url

                android.util.Log.d("ProfileSetupViewModel", "Profile image URL: $profileImage")

                // Split name into first and last name
                val nameParts = name.trim().split(" ", limit = 2)
                val firstName = nameParts.getOrNull(0) ?: name
                val lastName = nameParts.getOrNull(1) ?: ""

                android.util.Log.d("ProfileSetupViewModel", "First name: $firstName, Last name: $lastName")

                // Update profile with uploaded image URLs
                val result = updateProfileUseCase(
                    firstName = firstName,
                    lastName = lastName,
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    profileImageUrl = profileImage,
                    mode = mode
                )

                result.fold(
                    onSuccess = {
                        android.util.Log.d("ProfileSetupViewModel", "Profile updated successfully")
                        _uiState.value = UiState.Success(Unit)
                    },
                    onFailure = { error ->
                        android.util.Log.e("ProfileSetupViewModel", "Failed to update profile", error)
                        _uiState.value = UiState.Error(error.message ?: "Failed to update profile")
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("ProfileSetupViewModel", "Error in updateProfile", e)
                _uiState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
