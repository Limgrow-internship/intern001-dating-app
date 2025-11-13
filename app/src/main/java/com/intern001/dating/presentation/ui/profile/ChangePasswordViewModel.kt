package com.intern001.dating.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.model.request.ChangePasswordRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    @Named("datingApi") private val api: DatingApiService,
) : ViewModel() {

    private val _changePasswordState = MutableStateFlow<Result<Response<*>>?>(null)
    val changePasswordState = _changePasswordState.asStateFlow()

    fun changePassword(
        newPassword: String,
        confirmPassword: String,
    ) {
        viewModelScope.launch {
            try {
                val request = ChangePasswordRequest(
                    newPassword = newPassword,
                    confirmPassword = confirmPassword,
                    deviceInfo = "Android",
                )
                val response = api.changePassword(request)
                _changePasswordState.value = Result.success(response)
            } catch (e: Exception) {
                _changePasswordState.value = Result.failure(e)
            }
        }
    }
}
