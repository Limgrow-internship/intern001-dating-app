package com.intern001.dating.presentation.ui.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.domain.model.UpdateProfile
import com.intern001.dating.domain.usecase.auth.GetCurrentUserUseCase
import com.intern001.dating.domain.usecase.profile.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    sealed class UiState<out T> {
        object Idle : UiState<Nothing>()
        object Loading : UiState<Nothing>()
        data class Success<out T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }

    private val _userProfileState = MutableStateFlow<UiState<UpdateProfile>>(UiState.Idle)
    val userProfileState: StateFlow<UiState<UpdateProfile>> = _userProfileState

    private val _updateProfileState = MutableStateFlow<UiState<UpdateProfile>>(UiState.Idle)
    val updateProfileState: StateFlow<UiState<UpdateProfile>> = _updateProfileState

    fun getUserProfile() {
        viewModelScope.launch {
            _userProfileState.value = UiState.Loading
            val result = getCurrentUserUseCase()
            _userProfileState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun updateUserProfile(request: UpdateProfileRequest) {
        viewModelScope.launch {
            _updateProfileState.value = UiState.Loading
            val result = updateProfileUseCase(request)
            result.fold(
                onSuccess = { updatedProfile ->
                    // Update both states when profile is successfully updated
                    _updateProfileState.value = UiState.Success(updatedProfile)
                    _userProfileState.value = UiState.Success(updatedProfile)
                },
                onFailure = { error ->
                    _updateProfileState.value = UiState.Error(error.message ?: "Failed to update profile")
                }
            )
        }
    }

    fun updateGoal(goals: List<String>) {
        viewModelScope.launch {
            val request = UpdateProfileRequest(goals = goals)
            updateUserProfile(request)
        }
    }
}
