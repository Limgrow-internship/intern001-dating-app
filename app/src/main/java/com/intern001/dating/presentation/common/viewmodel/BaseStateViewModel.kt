package com.intern001.dating.presentation.common.viewmodel

import android.util.Log
import com.intern001.dating.presentation.common.state.UiState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class BaseStateViewModel<T> : BaseViewModel() {

    private val _uiState = MutableStateFlow<UiState<T>>(UiState.Idle)
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    protected val stateExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("BaseStateViewModel", "Coroutine error: ${exception.message}", exception)
        setError(exception.message ?: "Unknown error")
    }

    protected fun setLoading() {
        _uiState.value = UiState.Loading
    }

    protected fun setSuccess(data: T) {
        _uiState.value = UiState.Success(data)
    }

    protected fun setError(message: String) {
        _uiState.value = UiState.Error(message)
    }

    protected fun resetState() {
        _uiState.value = UiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        resetState()
    }
}
