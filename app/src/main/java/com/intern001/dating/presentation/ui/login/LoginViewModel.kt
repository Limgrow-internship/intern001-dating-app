package com.intern001.dating.presentation.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.response.UserBasicData
import com.intern001.dating.domain.model.AuthState
import com.intern001.dating.domain.model.User
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
    private val apiService: DatingApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AuthState>>(UiState.Idle)
    val uiState: StateFlow<UiState<AuthState>> = _uiState.asStateFlow()

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
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = apiService.googleLogin(mapOf("idToken" to idToken))

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        tokenManager.saveTokens(body.accessToken)

                        val authStateUser: User? = body.user?.let { userBasic: UserBasicData ->
                            User(
                                id = userBasic.id,
                                email = userBasic.email,
                                firstName = userBasic.firstName ?: "",
                                lastName = userBasic.lastName ?: "",
                                gender = null
                            )
                        }

                        _uiState.value = UiState.Success(
                            AuthState(
                                isLoggedIn = true,
                                token = body.accessToken,
                                user = authStateUser
                            )
                        )
                    } else {
                        _uiState.value = UiState.Error("Google login failed: empty response")
                    }
                } else {
                    _uiState.value = UiState.Error("Google login failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Google login failed")
            }
        }
    }
}
