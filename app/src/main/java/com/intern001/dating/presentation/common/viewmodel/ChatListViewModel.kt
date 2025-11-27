// ChatListViewModel.kt
package com.intern001.dating.presentation.common.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.domain.entity.LastMessageEntity
import com.intern001.dating.domain.model.MatchList
import com.intern001.dating.domain.usecase.GetLastMessageUseCase
import com.intern001.dating.domain.usecase.match.GetMatchedUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getMatchedUsersUseCase: GetMatchedUsersUseCase,
    private val getLastMessageUseCase: GetLastMessageUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    suspend fun getLastMessage(matchId: String): LastMessageEntity? {
        return withContext(Dispatchers.IO) {
            try {
                getLastMessageUseCase(matchId)
            } catch (e: Exception) {
                null
            }
        }
    }

    private val _matches = MutableStateFlow<List<MatchList>>(emptyList())
    val matches: StateFlow<List<MatchList>> = _matches

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchMatches(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getMatchedUsersUseCase(token)
                _matches.value = result
            } catch (e: Exception) {
                _matches.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
