package com.intern001.dating.presentation.common.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.domain.model.VerificationResult
import com.intern001.dating.domain.usecase.profile.VerifyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val verifyProfileUseCase: VerifyProfileUseCase,
) : ViewModel() {
    private val _verificationResult = MutableLiveData<VerificationResult>()
    val verificationResult: LiveData<VerificationResult> = _verificationResult

    fun verifyFace(imageBytes: ByteArray) {
        viewModelScope.launch {
            val result = verifyProfileUseCase.execute(imageBytes)
            if (result.isSuccess) {
                _verificationResult.postValue(result.getOrNull())
            } else {
                _verificationResult.postValue(VerificationResult(false, result.exceptionOrNull()?.message))
            }
        }
    }
}
