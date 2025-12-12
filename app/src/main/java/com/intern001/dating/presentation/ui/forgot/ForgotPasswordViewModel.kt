package com.intern001.dating.presentation.ui.forgot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.domain.usecase.forgot.ForgotPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
) : ViewModel() {

    sealed class ForgotState {
        object Idle : ForgotState()
        object Loading : ForgotState()
        object OtpSent : ForgotState()
        data class OtpVerified(val otpToken: String) : ForgotState()
        object Success : ForgotState()
        data class Error(val message: String) : ForgotState()
    }

    private val _state = MutableLiveData<ForgotState>(ForgotState.Idle)
    val state: LiveData<ForgotState> = _state

    fun requestOtp(email: String) {
        viewModelScope.launch {
            _state.value = ForgotState.Loading

            runCatching {
                forgotPasswordUseCase.requestOtp(email)
            }.onSuccess {
                _state.value = ForgotState.OtpSent
            }.onFailure { e ->
                _state.value = ForgotState.Error(
                    e.message ?: "Email not found",
                )
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            _state.value = ForgotState.Loading
            runCatching {
                forgotPasswordUseCase.verifyOtp(email, otp)
            }.onSuccess {
                _state.value = ForgotState.OtpVerified(it)
            }.onFailure {
                _state.value = ForgotState.Error(it.message ?: "Error")
            }
        }
    }

    fun resetPassword(
        otpToken: String,
        newPassword: String,
        confirmPassword: String,
    ) {
        viewModelScope.launch {
            _state.value = ForgotState.Loading
            runCatching {
                forgotPasswordUseCase.resetPassword(
                    otpToken,
                    newPassword,
                    confirmPassword,
                )
            }.onSuccess {
                _state.value = ForgotState.Success
            }.onFailure {
                _state.value = ForgotState.Error(it.message ?: "Error")
            }
        }
    }

    fun resetState() {
        _state.value = ForgotState.Idle
    }
}
