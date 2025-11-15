package com.intern001.dating.presentation.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.model.response.FacebookLoginResponse
import com.intern001.dating.domain.model.AuthState
import com.intern001.dating.domain.usecase.auth.FacebookLoginUseCase
import com.intern001.dating.domain.usecase.auth.LoginUseCase
import com.intern001.dating.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val facebookLoginUseCase: FacebookLoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AuthState>>(UiState.Idle)
    val uiState: StateFlow<UiState<AuthState>> = _uiState.asStateFlow()

    private val _fbUiState = MutableStateFlow<UiState<FacebookLoginResponse>>(UiState.Idle)
    val fbUiState: StateFlow<UiState<FacebookLoginResponse>> = _fbUiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val result = loginUseCase(email, password)

            _uiState.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    fun loginWithFacebook(fbToken: String) {
        viewModelScope.launch {
            _fbUiState.value = UiState.Loading
            val result = facebookLoginUseCase(fbToken)
            _fbUiState.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Facebook login error")
            }
        }
    }
}
