package com.intern001.dating.presentation.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.domain.model.MatchStatusGet
import com.intern001.dating.domain.usecase.match.GetMatchStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MatchStatusViewModel @Inject constructor(
    private val getMatchStatusUseCase: GetMatchStatusUseCase,
) : ViewModel() {

    private val _matchStatus = MutableLiveData<MatchStatusGet>()
    val matchStatus: LiveData<MatchStatusGet> = _matchStatus

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMatchStatus(targetUserId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val result = getMatchStatusUseCase(targetUserId)
                _matchStatus.value = result
                _loading.value = false
            } catch (e: Exception) {
                _loading.value = false
                _error.value = e.message
            }
        }
    }
}
