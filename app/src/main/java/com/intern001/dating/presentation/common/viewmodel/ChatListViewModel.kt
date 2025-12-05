// ChatListViewModel.kt
package com.intern001.dating.presentation.common.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.local.TokenManager
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

    /**
     * Lấy last message cho một matchId
     * Kiểm tra cache trước, nếu không có mới fetch từ server
     */
    suspend fun getLastMessage(matchId: String): LastMessageEntity? {
        // Kiểm tra cache trước
        val cached = _lastMessagesCache.value[matchId]
        if (cached != null) {
            return cached
        }

        // Nếu chưa có trong cache, fetch từ server và cache lại
        return withContext(Dispatchers.IO) {
            try {
                val lastMsg = getLastMessageUseCase(matchId)
                if (lastMsg != null) {
                    // Cache lại để lần sau không cần fetch
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

    /**
     * Preload last messages cho tất cả matches
     * Được gọi sau khi matches đã được load
     */
    fun preloadLastMessages() {
        viewModelScope.launch {
            val matches = _matches.value
            if (matches.isEmpty()) return@launch

            matches.forEach { match ->
                // Chỉ fetch nếu chưa có trong cache
                if (!_lastMessagesCache.value.containsKey(match.matchId)) {
                    try {
                        val lastMsg = getLastMessageUseCase(match.matchId)
                        if (lastMsg != null) {
                            val currentCache = _lastMessagesCache.value.toMutableMap()
                            currentCache[match.matchId] = lastMsg
                            _lastMessagesCache.value = currentCache
                        }
                    } catch (e: Exception) {
                        // Ignore errors khi preload
                    }
                }
            }
        }
    }

    /**
     * Update last message cho một matchId (khi có message mới)
     */
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

    // Cache last messages theo matchId để tránh fetch lại mỗi lần vào màn hình
    private val _lastMessagesCache = MutableStateFlow<Map<String, LastMessageEntity>>(emptyMap())
    val lastMessagesCache: StateFlow<Map<String, LastMessageEntity>> = _lastMessagesCache

    private var hasPreloaded = false

    /**
     * Fetch matches với token từ TokenManager
     */
    fun fetchMatches() {
        viewModelScope.launch {
            val token = tokenManager.getAccessTokenAsync() ?: return@launch
            fetchMatches(token)
        }
    }

    /**
     * Fetch matches với token được cung cấp
     */
    fun fetchMatches(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getMatchedUsersUseCase(token)
                _matches.value = result
                hasPreloaded = true
                android.util.Log.d("ChatListViewModel", "Fetched ${result.size} matches")

                // Preload last messages sau khi có matches
                preloadLastMessages()
            } catch (e: Exception) {
                android.util.Log.e("ChatListViewModel", "Failed to fetch matches", e)
                _matches.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Preload matches nếu chưa được load
     */
    fun preloadMatches() {
        if (hasPreloaded || _isLoading.value) {
            return
        }
        fetchMatches()
    }

    /**
     * Kiểm tra xem đã có data chưa
     */
    fun hasData(): Boolean {
        return _matches.value.isNotEmpty()
    }
}
