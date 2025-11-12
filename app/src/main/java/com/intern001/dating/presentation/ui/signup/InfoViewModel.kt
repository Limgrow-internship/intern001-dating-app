package com.intern001.dating.presentation.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.model.request.RequestOtpRequest
import com.intern001.dating.data.model.request.VerifyOtpRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.launch

@HiltViewModel
class InfoViewModel @Inject constructor(
    @Named("datingApi") private val apiService: DatingApiService,
) : ViewModel() {

    fun sendOtp(email: String, password: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.requestOtp(RequestOtpRequest(email, password))
                if (response.isSuccessful) {
                    onResult(response.body()?.message ?: "Success")
                } else {
                    onResult("Failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                onResult("Error: ${e.message}")
            }
        }
    }

    fun verifyOtp(email: String, otp: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = VerifyOtpRequest(email, otp)
                val response = apiService.verifyOtp(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val message = body?.message ?: "Verified"

                    onResult(message)
                } else {
                    onResult("Failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                onResult("Error: ${e.message}")
            }
        }
    }
}
