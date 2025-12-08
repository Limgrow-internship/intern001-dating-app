package com.intern001.dating.presentation.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.model.response.ReportResponse
import com.intern001.dating.domain.usecase.ReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val useCase: ReportUseCase,
) : ViewModel() {

    private val _reportResult = MutableStateFlow<ReportResponse?>(null)
    val reportResult = _reportResult

    private val _error = MutableStateFlow<String?>(null)
    val error = _error

    private val _loading = MutableStateFlow(false)
    val loading = _loading

    fun submitReport(userIdIsReported: String, reason: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ReportVM", "submitReport() userIdIsReported=$userIdIsReported reason=$reason")

                _loading.value = true

                val result = useCase(userIdIsReported, reason)

                result.onSuccess {
                    android.util.Log.d("ReportVM", "SUCCESS => $it")
                    _reportResult.value = it
                }
                result.onFailure {
                    android.util.Log.e("ReportVM", "ERROR => ${it.message}")
                    _error.value = it.message
                }
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearResult() {
        _reportResult.value = null
    }
    fun clearError() {
        _error.value = null
    }
}
