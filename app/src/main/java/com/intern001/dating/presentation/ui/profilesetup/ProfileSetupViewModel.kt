package com.intern001.dating.presentation.ui.profilesetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.domain.usecase.profile.UpdateProfileUseCase
import com.intern001.dating.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    // Profile data
    var photoUrl: String? = null
    var name: String = ""
    var gender: String = ""
    var dateOfBirth: String = ""
    var goal: String = ""

    fun updateProfile() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val result = updateProfileUseCase(
                firstName = name.split(" ").firstOrNull() ?: name,
                lastName = name.split(" ").drop(1).joinToString(" ").ifEmpty { "" },
                dateOfBirth = dateOfBirth,
                gender = gender,
                profileImageUrl = photoUrl,
                goal = goal
            )

            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Success(Unit)
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to update profile")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
