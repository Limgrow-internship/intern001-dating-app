package com.intern001.dating.presentation.common.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.domain.model.MatchList
import com.intern001.dating.domain.usecase.match.GetMatchedUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getMatchedUsersUseCase: GetMatchedUsersUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _matches = MutableLiveData<List<MatchList>>()
    val matches: LiveData<List<MatchList>> = _matches

    fun fetchMatches(token: String) {
        viewModelScope.launch {
            try {
                val result = getMatchedUsersUseCase(token)
                _matches.value = result
            } catch (e: Exception) {
            }
        }
    }
}
