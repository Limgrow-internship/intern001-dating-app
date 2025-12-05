package com.intern001.dating.presentation.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.local.ChatLocalRepository
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ChatSharedViewModel @Inject constructor(
    private val repo: ChatRepository,
    private val localRepo: ChatLocalRepository,
) : ViewModel() {

    private val _messagesCache = MutableStateFlow<Map<String, List<MessageModel>>>(emptyMap())
    val messagesCache: StateFlow<Map<String, List<MessageModel>>> = _messagesCache

    private val _preloadingMatchIds = MutableStateFlow<Set<String>>(emptySet())
    val preloadingMatchIds: StateFlow<Set<String>> = _preloadingMatchIds

    fun preloadMessages(matchId: String) {
        if (_messagesCache.value.containsKey(matchId) || _preloadingMatchIds.value.contains(matchId)) {
            return
        }

        viewModelScope.launch {
            try {
                _preloadingMatchIds.value = _preloadingMatchIds.value + matchId

                try {
                    val localMessages = localRepo.getMessagesByMatchIdSync(matchId)
                    if (localMessages.isNotEmpty()) {
                        val currentCache = _messagesCache.value.toMutableMap()
                        currentCache[matchId] = localMessages
                        _messagesCache.value = currentCache
                        android.util.Log.d("ChatSharedViewModel", "Loaded ${localMessages.size} messages from local DB for matchId: $matchId")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChatSharedViewModel", "Failed to load from local DB", e)
                }

                try {
                    val allMsgs = repo.getHistory(matchId)
                    val deliveredMsgs = allMsgs.filter { it.delivered == true }

                    localRepo.saveMessages(deliveredMsgs)

                    val currentCache = _messagesCache.value.toMutableMap()
                    currentCache[matchId] = deliveredMsgs
                    _messagesCache.value = currentCache

                    android.util.Log.d("ChatSharedViewModel", "Preloaded ${deliveredMsgs.size} messages from server for matchId: $matchId")
                } catch (e: Exception) {
                    android.util.Log.w("ChatSharedViewModel", "Failed to fetch from server, using local cache if available", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatSharedViewModel", "Failed to preload messages for matchId: $matchId", e)
            } finally {
                _preloadingMatchIds.value = _preloadingMatchIds.value - matchId
            }
        }
    }

    fun getCachedMessages(matchId: String): List<MessageModel> {
        return _messagesCache.value[matchId] ?: emptyList()
    }

    suspend fun getCachedMessagesAsync(matchId: String): List<MessageModel> {
        val memoryCache = _messagesCache.value[matchId]
        if (memoryCache != null && memoryCache.isNotEmpty()) {
            return memoryCache
        }

        return try {
            val localMessages = localRepo.getMessagesByMatchIdSync(matchId)
            if (localMessages.isNotEmpty()) {
                val currentCache = _messagesCache.value.toMutableMap()
                currentCache[matchId] = localMessages
                _messagesCache.value = currentCache
            }
            localMessages
        } catch (e: Exception) {
            android.util.Log.e("ChatSharedViewModel", "Failed to load from local DB", e)
            emptyList()
        }
    }

    fun updateMessages(matchId: String, messages: List<MessageModel>) {
        val currentCache = _messagesCache.value.toMutableMap()
        currentCache[matchId] = messages
        _messagesCache.value = currentCache

        viewModelScope.launch {
            try {
                localRepo.saveMessages(messages)
                android.util.Log.d("ChatSharedViewModel", "Saved ${messages.size} messages to local DB for matchId: $matchId")
            } catch (e: Exception) {
                android.util.Log.e("ChatSharedViewModel", "Failed to save messages to local DB", e)
            }
        }
    }

    fun clearCache(matchId: String) {
        val currentCache = _messagesCache.value.toMutableMap()
        currentCache.remove(matchId)
        _messagesCache.value = currentCache
    }

    fun fetchAndCacheMessages(matchId: String, onComplete: (List<MessageModel>) -> Unit = {}) {
        val cached = _messagesCache.value[matchId]
        if (cached != null && cached.isNotEmpty()) {
            onComplete(cached)
            return
        }

        viewModelScope.launch {
            try {
                val localMessages = localRepo.getMessagesByMatchIdSync(matchId)
                if (localMessages.isNotEmpty()) {
                    val currentCache = _messagesCache.value.toMutableMap()
                    currentCache[matchId] = localMessages
                    _messagesCache.value = currentCache
                    onComplete(localMessages)
                }

                try {
                    val allMsgs = repo.getHistory(matchId)
                    val deliveredMsgs = allMsgs.filter { it.delivered == true }

                    localRepo.saveMessages(deliveredMsgs)

                    val currentCache = _messagesCache.value.toMutableMap()
                    currentCache[matchId] = deliveredMsgs
                    _messagesCache.value = currentCache

                    onComplete(deliveredMsgs)
                } catch (e: Exception) {
                    android.util.Log.w("ChatSharedViewModel", "Failed to fetch from server", e)
                    if (localMessages.isEmpty()) {
                        onComplete(emptyList())
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatSharedViewModel", "Failed to fetch messages for matchId: $matchId", e)
                onComplete(emptyList())
            }
        }
    }
}
