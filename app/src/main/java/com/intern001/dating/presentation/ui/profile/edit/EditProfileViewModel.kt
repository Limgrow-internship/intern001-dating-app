package com.intern001.dating.presentation.ui.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.domain.cache.InitialDataCache
import com.intern001.dating.domain.model.EnhancedBioResult
import com.intern001.dating.domain.model.UpdateProfile
import com.intern001.dating.domain.usecase.auth.GetCurrentUserUseCase
import com.intern001.dating.domain.usecase.profile.EnhanceBioUseCase
import com.intern001.dating.domain.usecase.profile.GenerateBioUseCase
import com.intern001.dating.domain.usecase.profile.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val generateBioUseCase: GenerateBioUseCase,
    private val enhanceBioUseCase: EnhanceBioUseCase,
    private val initialDataCache: InitialDataCache,
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

    private val _generateBioState = MutableStateFlow<UiState<UpdateProfile>>(UiState.Idle)
    val generateBioState: StateFlow<UiState<UpdateProfile>> = _generateBioState

    private val _enhanceBioState = MutableStateFlow<UiState<EnhancedBioResult>>(UiState.Idle)
    val enhanceBioState: StateFlow<UiState<EnhancedBioResult>> = _enhanceBioState

    fun getUserProfile(forceRefresh: Boolean = false) {
        if (!forceRefresh) {
            initialDataCache.consumeUserProfile()?.let { cachedProfile ->
                _userProfileState.value = UiState.Success(cachedProfile)
                fetchUserProfile(silent = true)
                return
            }
        }
        fetchUserProfile(silent = false)
    }

    fun updateUserProfile(request: UpdateProfileRequest) {
        viewModelScope.launch {
            _updateProfileState.value = UiState.Loading
            val result = updateProfileUseCase(request)
            result.fold(
                onSuccess = { updatedProfile ->
                    initialDataCache.storeUserProfile(updatedProfile)
                    // Update both states when profile is successfully updated
                    _updateProfileState.value = UiState.Success(updatedProfile)
                    _userProfileState.value = UiState.Success(updatedProfile)
                },
                onFailure = { error ->
                    _updateProfileState.value = UiState.Error(error.message ?: "Failed to update profile")
                },
            )
        }
    }

    fun generateBio(prompt: String) {
        viewModelScope.launch {
            _generateBioState.value = UiState.Loading
            val result = generateBioUseCase(prompt)
            result.fold(
                onSuccess = { updatedProfile ->
                    initialDataCache.storeUserProfile(updatedProfile)
                    _generateBioState.value = UiState.Success(updatedProfile)
                    _userProfileState.value = UiState.Success(updatedProfile)
                },
                onFailure = { error ->
                    _generateBioState.value = UiState.Error(error.message ?: "Failed to generate bio")
                },
            )
        }
    }

    fun enhanceBio() {
        viewModelScope.launch {
            _enhanceBioState.value = UiState.Loading
            val result = enhanceBioUseCase()
            result.fold(
                onSuccess = { enhanced ->
                    _enhanceBioState.value = UiState.Success(enhanced)
                },
                onFailure = { error ->
                    _enhanceBioState.value = UiState.Error(error.message ?: "Failed to enhance bio")
                },
            )
        }
    }

    private fun fetchUserProfile(silent: Boolean) {
        viewModelScope.launch {
            if (!silent) {
                _userProfileState.value = UiState.Loading
            }
            val result = getCurrentUserUseCase()
            _userProfileState.value =
                result.fold(
                    onSuccess = {
                        initialDataCache.storeUserProfile(it)
                        UiState.Success(it)
                    },
                    onFailure = { UiState.Error(it.message ?: "Unknown error") },
                )
        }
    }
}
