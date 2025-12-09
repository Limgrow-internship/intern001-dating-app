package com.intern001.dating.presentation.common.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.local.prefs.TokenManager
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
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    suspend fun getLastMessage(matchId: String): LastMessageEntity? {
        val cached = _lastMessagesCache.value[matchId]
        if (cached != null) {
            return cached
        }

        return withContext(Dispatchers.IO) {
            try {
                val lastMsg = getLastMessageUseCase(matchId)
                if (lastMsg != null) {
                    val currentCache = _lastMessagesCache.value.toMutableMap()
                    currentCache[matchId] = lastMsg
                    _lastMessagesCache.value = currentCache
                }
                lastMsg
            } catch (e: Exception) {
                null
            }
        }
    }

    fun preloadLastMessages() {
        viewModelScope.launch {
            val matches = _matches.value
            if (matches.isEmpty()) return@launch

            matches.forEach { match ->
                if (!_lastMessagesCache.value.containsKey(match.matchId)) {
                    try {
                        val lastMsg = getLastMessageUseCase(match.matchId)
                        if (lastMsg != null) {
                            val currentCache = _lastMessagesCache.value.toMutableMap()
                            currentCache[match.matchId] = lastMsg
                            _lastMessagesCache.value = currentCache
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    fun updateLastMessage(matchId: String, lastMessage: LastMessageEntity?) {
        val currentCache = _lastMessagesCache.value.toMutableMap()
        if (lastMessage != null) {
            currentCache[matchId] = lastMessage
        } else {
            currentCache.remove(matchId)
        }
        _lastMessagesCache.value = currentCache
    }

    private val _matches = MutableStateFlow<List<MatchList>>(emptyList())
    val matches: StateFlow<List<MatchList>> = _matches

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _lastMessagesCache = MutableStateFlow<Map<String, LastMessageEntity>>(emptyMap())
    val lastMessagesCache: StateFlow<Map<String, LastMessageEntity>> = _lastMessagesCache

    private var hasPreloaded = false

    fun fetchMatches() {
        viewModelScope.launch {
            val token = tokenManager.getAccessTokenAsync() ?: return@launch
            // Only show loading state if we have nothing to display yet
            fetchMatches(token, showLoading = _matches.value.isEmpty())
        }
    }

    fun refreshMatches() {
        viewModelScope.launch {
            val token = tokenManager.getAccessTokenAsync() ?: return@launch
            // Silent refresh to avoid UI flicker when data already exists
            fetchMatches(token, showLoading = false)
        }
    }

    fun fetchMatches(token: String, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) _isLoading.value = true
            try {
                val result = getMatchedUsersUseCase(token)
                _matches.value = result
                hasPreloaded = true
                android.util.Log.d("ChatListViewModel", "Fetched ${result.size} matches")

                preloadLastMessages()
            } catch (e: Exception) {
                android.util.Log.e("ChatListViewModel", "Failed to fetch matches", e)
                if (showLoading) _matches.value = emptyList()
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }

    fun preloadMatches() {
        if (hasPreloaded || _isLoading.value) {
            return
        }
        fetchMatches()
    }

    fun hasData(): Boolean {
        return _matches.value.isNotEmpty()
    }
}
