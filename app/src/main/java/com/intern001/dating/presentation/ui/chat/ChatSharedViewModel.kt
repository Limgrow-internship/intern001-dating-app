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

/**
 * Activity-scoped ViewModel để cache và preload messages
 * Share giữa ChatListFragment và ChatDetailFragment
 *
 * Flow hoạt động:
 * 1. Khi preload messages:
 *    - Đầu tiên load từ local database (nếu có) → hiển thị ngay
 *    - Sau đó fetch từ server → update local DB và cache
 *
 * 2. Khi offline hoặc server lỗi:
 *    - Load từ local database → vẫn có thể xem messages đã load trước đó
 *
 * 3. Khi có message mới:
 *    - Update memory cache → hiển thị ngay
 *    - Save vào local database → persist để xem offline sau này
 */
@HiltViewModel
class ChatSharedViewModel @Inject constructor(
    private val repo: ChatRepository,
    private val localRepo: ChatLocalRepository, // Inject local repository để persist messages
) : ViewModel() {

    // Cache messages theo matchId
    private val _messagesCache = MutableStateFlow<Map<String, List<MessageModel>>>(emptyMap())
    val messagesCache: StateFlow<Map<String, List<MessageModel>>> = _messagesCache

    // Track các matchId đang được preload
    private val _preloadingMatchIds = MutableStateFlow<Set<String>>(emptySet())
    val preloadingMatchIds: StateFlow<Set<String>> = _preloadingMatchIds

    /**
     * Preload messages cho một matchId
     * Được gọi từ ChatListFragment khi user click vào conversation
     *
     * Flow:
     * 1. Kiểm tra memory cache → nếu có thì return
     * 2. Load từ local database → hiển thị ngay (offline support)
     * 3. Fetch từ server → update local DB và cache
     */
    fun preloadMessages(matchId: String) {
        // Nếu đã có trong cache hoặc đang preload, không cần load lại
        if (_messagesCache.value.containsKey(matchId) || _preloadingMatchIds.value.contains(matchId)) {
            return
        }

        viewModelScope.launch {
            try {
                _preloadingMatchIds.value = _preloadingMatchIds.value + matchId

                // Bước 1: Thử load từ local database trước (offline support)
                try {
                    val localMessages = localRepo.getMessagesByMatchIdSync(matchId)
                    if (localMessages.isNotEmpty()) {
                        // Có messages trong local DB, sử dụng luôn để hiển thị ngay
                        val currentCache = _messagesCache.value.toMutableMap()
                        currentCache[matchId] = localMessages
                        _messagesCache.value = currentCache
                        android.util.Log.d("ChatSharedViewModel", "Loaded ${localMessages.size} messages from local DB for matchId: $matchId")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChatSharedViewModel", "Failed to load from local DB", e)
                }

                // Bước 2: Fetch từ server để update (nếu có mạng)
                try {
                    val allMsgs = repo.getHistory(matchId)
                    val deliveredMsgs = allMsgs.filter { it.delivered == true }

                    // Save vào local database để persist
                    localRepo.saveMessages(deliveredMsgs)

                    // Update memory cache
                    val currentCache = _messagesCache.value.toMutableMap()
                    currentCache[matchId] = deliveredMsgs
                    _messagesCache.value = currentCache

                    android.util.Log.d("ChatSharedViewModel", "Preloaded ${deliveredMsgs.size} messages from server for matchId: $matchId")
                } catch (e: Exception) {
                    // Nếu server lỗi hoặc không có mạng, vẫn có thể dùng messages từ local DB
                    android.util.Log.w("ChatSharedViewModel", "Failed to fetch from server, using local cache if available", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatSharedViewModel", "Failed to preload messages for matchId: $matchId", e)
            } finally {
                _preloadingMatchIds.value = _preloadingMatchIds.value - matchId
            }
        }
    }

    /**
     * Lấy messages từ cache (sync - chỉ check memory cache)
     * Dùng khi cần check nhanh trong memory
     */
    fun getCachedMessages(matchId: String): List<MessageModel> {
        return _messagesCache.value[matchId] ?: emptyList()
    }

    /**
     * Lấy messages từ local database (async)
     * Dùng khi cần load từ DB mà không block
     */
    suspend fun getCachedMessagesAsync(matchId: String): List<MessageModel> {
        // Kiểm tra memory cache trước
        val memoryCache = _messagesCache.value[matchId]
        if (memoryCache != null && memoryCache.isNotEmpty()) {
            return memoryCache
        }

        // Load từ local DB
        return try {
            val localMessages = localRepo.getMessagesByMatchIdSync(matchId)
            if (localMessages.isNotEmpty()) {
                // Update memory cache
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

    /**
     * Update messages trong cache (khi có message mới)
     * Cập nhật cả memory cache và local database
     */
    fun updateMessages(matchId: String, messages: List<MessageModel>) {
        // Update memory cache
        val currentCache = _messagesCache.value.toMutableMap()
        currentCache[matchId] = messages
        _messagesCache.value = currentCache

        // Persist vào local database để có thể xem offline sau này
        viewModelScope.launch {
            try {
                localRepo.saveMessages(messages)
                android.util.Log.d("ChatSharedViewModel", "Saved ${messages.size} messages to local DB for matchId: $matchId")
            } catch (e: Exception) {
                android.util.Log.e("ChatSharedViewModel", "Failed to save messages to local DB", e)
            }
        }
    }

    /**
     * Clear cache cho một matchId
     */
    fun clearCache(matchId: String) {
        val currentCache = _messagesCache.value.toMutableMap()
        currentCache.remove(matchId)
        _messagesCache.value = currentCache
    }

    /**
     * Fetch messages và update cache (fallback nếu chưa có trong cache)
     * Flow:
     * 1. Kiểm tra memory cache → nếu có thì return
     * 2. Thử load từ local DB → nếu có thì return
     * 3. Fetch từ server → update cả memory cache và local DB
     */
    fun fetchAndCacheMessages(matchId: String, onComplete: (List<MessageModel>) -> Unit = {}) {
        // Kiểm tra memory cache trước
        val cached = _messagesCache.value[matchId]
        if (cached != null && cached.isNotEmpty()) {
            onComplete(cached)
            return
        }

        viewModelScope.launch {
            try {
                // Thử load từ local DB
                val localMessages = localRepo.getMessagesByMatchIdSync(matchId)
                if (localMessages.isNotEmpty()) {
                    // Update memory cache
                    val currentCache = _messagesCache.value.toMutableMap()
                    currentCache[matchId] = localMessages
                    _messagesCache.value = currentCache
                    onComplete(localMessages)
                }

                // Fetch từ server để update
                try {
                    val allMsgs = repo.getHistory(matchId)
                    val deliveredMsgs = allMsgs.filter { it.delivered == true }

                    // Save vào local database
                    localRepo.saveMessages(deliveredMsgs)

                    // Update memory cache
                    val currentCache = _messagesCache.value.toMutableMap()
                    currentCache[matchId] = deliveredMsgs
                    _messagesCache.value = currentCache

                    onComplete(deliveredMsgs)
                } catch (e: Exception) {
                    // Nếu server lỗi, vẫn có thể return messages từ local DB (nếu có)
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
