package com.intern001.dating.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.domain.model.LikedYouUser
import com.intern001.dating.domain.usecase.match.GetUsersWhoLikedYouUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LikedYouViewModel @Inject constructor(
    private val getUsersWhoLikedYouUseCase: GetUsersWhoLikedYouUseCase
) : ViewModel() {

    sealed class UiState<out T> {
        object Idle : UiState<Nothing>()
        object Loading : UiState<Nothing>()
        data class Success<out T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }

    private val _likedYouState =
        MutableStateFlow<UiState<List<LikedYouUser>>>(UiState.Idle)
    val likedYouState: StateFlow<UiState<List<LikedYouUser>>> = _likedYouState

    fun loadUsersWhoLikedMe() {
        _likedYouState.value = UiState.Loading

        viewModelScope.launch {
            val result = getUsersWhoLikedYouUseCase()

            _likedYouState.value = result.fold(
                onSuccess = { list ->
                    UiState.Success(list)
                },
                onFailure = { error ->
                    UiState.Error(error.message ?: "Failed to load users who liked you")
                }
            )
        }
    }
}
