package com.intern001.dating.presentation.common.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.response.PhotoResponse
import com.intern001.dating.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {
    private val _uploadResult = MutableLiveData<PhotoResponse?>()
    val uploadResult: LiveData<PhotoResponse?> get() = _uploadResult

    fun uploadSelfie(imageBytes: ByteArray) {
        val token = tokenManager.getAccessToken() ?: return
        viewModelScope.launch {
            val result = userRepository.uploadSelfie(imageBytes)
            if (result.isSuccess) {
                _uploadResult.postValue(result.getOrNull())
            } else {
                _uploadResult.postValue(null)
            }
        }
    }
}
