package com.intern001.dating.presentation.ui.chat

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.local.ChatLocalRepository
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.data.service.ChatSocketService
import com.intern001.dating.domain.repository.ChatRepository
import com.intern001.dating.domain.usecase.SendVoiceMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository,
    private val sendVoiceMessageUseCase: SendVoiceMessageUseCase,
    private val tokenManager: TokenManager,
    private val localRepo: ChatLocalRepository, // Inject local repository để persist messages
) : ViewModel() {
    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages

    private val _matchStatus = MutableStateFlow("active")
    val matchStatus: StateFlow<String> = _matchStatus
    private val _blockerId = MutableStateFlow<String?>(null)
    val blockerId: StateFlow<String?> = _blockerId

    private var socketService: ChatSocketService? = null

    private val _isAITyping = MutableStateFlow<Boolean>(false)
    val isAITyping: StateFlow<Boolean> = _isAITyping

    private val typingTimeoutHandler = Handler(Looper.getMainLooper())
    private var typingTimeoutRunnable: Runnable? = null

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isSocketConnected = MutableStateFlow<Boolean>(false)
    val isSocketConnected: StateFlow<Boolean> = _isSocketConnected

    var currentMatchId: String = ""
    var currentUserId: String = ""
    var isAIConversation: Boolean = false

    fun fetchHistory(matchId: String) {
        viewModelScope.launch {
            try {
                val allMsgs = repo.getHistory(matchId)
                val deliveredMsgs = allMsgs.filter { it.delivered == true }

                val currentMsgs = _messages.value
                val mergedMsgs = mergeMessages(currentMsgs, deliveredMsgs)
                _messages.value = mergedMsgs

                // Persist messages vào local database để có thể xem offline sau này
                try {
                    localRepo.saveMessages(mergedMsgs)
                    android.util.Log.d("ChatViewModel", "Saved ${mergedMsgs.size} messages to local DB")
                } catch (e: Exception) {
                    android.util.Log.e("ChatViewModel", "Failed to save messages to local DB", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Failed to fetch history", e)
                // Nếu server lỗi, thử load từ local DB
                try {
                    val localMessages = localRepo.getMessagesByMatchIdSync(matchId)
                    if (localMessages.isNotEmpty()) {
                        android.util.Log.d("ChatViewModel", "Loaded ${localMessages.size} messages from local DB (offline)")
                        _messages.value = localMessages
                    }
                } catch (localError: Exception) {
                    android.util.Log.e("ChatViewModel", "Failed to load from local DB", localError)
                }
            }
        }
    }

    /**
     * Update messages từ cache (được gọi khi có messages đã được preload)
     */
    fun updateMessagesFromCache(cachedMessages: List<MessageModel>) {
        val currentMsgs = _messages.value
        val mergedMsgs = mergeMessages(currentMsgs, cachedMessages)
        _messages.value = mergedMsgs
        android.util.Log.d("ChatViewModel", "Updated messages from cache: ${mergedMsgs.size} messages")
    }

    private fun mergeMessages(current: List<MessageModel>, new: List<MessageModel>): List<MessageModel> {
        val merged = mutableListOf<MessageModel>()
        val seen = mutableSetOf<String>()

        new.forEach { msg ->
            val key = "${msg.senderId}_${msg.message}_${msg.matchId}_${msg.imgChat}_${msg.audioPath}"
            if (!seen.contains(key)) {
                merged.add(msg)
                seen.add(key)
            }
        }

        current.forEach { msg ->
            val key = "${msg.senderId}_${msg.message}_${msg.matchId}_${msg.imgChat}_${msg.audioPath}"
            if (!seen.contains(key)) {
                merged.add(msg)
                seen.add(key)
            }
        }

        return merged.sortedBy { it.timestamp ?: "" }
    }

    fun sendMessage(message: MessageModel) {
        viewModelScope.launch {
            try {
                repo.sendMessage(message)
                fetchHistory(message.matchId)
            } catch (_: Exception) { }
        }
    }

    fun sendVoiceMessage(matchId: String, senderId: String, localAudioPath: String, duration: Int) {
        viewModelScope.launch {
            sendVoiceMessageUseCase(matchId, senderId, localAudioPath, duration)
            fetchHistory(matchId)
        }
    }

    fun sendImageMessage(imageUrl: String, matchId: String, senderId: String) {
        viewModelScope.launch {
            val message = MessageModel(
                senderId = senderId,
                matchId = matchId,
                message = "",
                imgChat = imageUrl,
            )
            repo.sendMessage(message)
            fetchHistory(matchId)
        }
    }
    suspend fun uploadChatImage(file: MultipartBody.Part): String? {
        return try {
            repo.uploadImage(file)?.url?.secure_url
        } catch (e: Exception) {
            null
        }
    }
    fun deleteAllMessages(matchId: String) {
        viewModelScope.launch {
            try {
                repo.deleteAllMessages(matchId)
                fetchHistory(matchId)
            } catch (_: Exception) { }
        }
    }

    fun unmatch(targetUserId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repo.unmatch(targetUserId)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun blockUser(targetUserId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repo.block(targetUserId)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun unblockUser(targetUserId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repo.unblock(targetUserId)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
    fun fetchMatchStatus(targetUserId: String) {
        viewModelScope.launch {
            try {
                val resp = repo.getMatchStatusResponse(targetUserId)
                _matchStatus.value = resp.status
                _blockerId.value = resp.blockerId
            } catch (e: Exception) {
                _matchStatus.value = "error"
                _blockerId.value = null
            }
        }
    }

    fun addMessage(message: MessageModel) {
        val currentMessages = _messages.value
        val key = "${message.senderId}_${message.message}_${message.matchId}_${message.imgChat}_${message.audioPath}"
        val exists = currentMessages.any {
            "${it.senderId}_${it.message}_${it.matchId}_${it.imgChat}_${it.audioPath}" == key
        }

        if (!exists) {
            val updatedMessages = currentMessages + message
            _messages.value = updatedMessages.sortedBy { it.timestamp ?: "" }

            android.util.Log.d("ChatViewModel", "Added message: ${message.message.take(50)}... Total: ${_messages.value.size}")

            if (isAIConversation && com.intern001.dating.presentation.ui.chat.AIConstants.isMessageFromAI(message.senderId)) {
                hideAITypingIndicator()
            }
        } else {
            android.util.Log.d("ChatViewModel", "Message already exists, skipping: ${message.message.take(50)}...")
        }
    }

    fun initializeSocket() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessTokenAsync() ?: ""
                if (token.isBlank()) {
                    android.util.Log.e("ChatViewModel", "No authentication token")
                    _error.value = "No authentication token"
                    return@launch
                }

                android.util.Log.d("ChatViewModel", "Initializing socket with token: ${token.take(20)}...")
                socketService = ChatSocketService(token).apply {
                    onConnectionStatusChanged = { isConnected ->
                        android.util.Log.d("ChatViewModel", "Socket connection status changed: $isConnected")
                        _isSocketConnected.value = isConnected
                    }
                    connect()
                }
                android.util.Log.d("ChatViewModel", "Socket service created and connecting...")
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Failed to initialize socket", e)
                _error.value = "Failed to initialize socket: ${e.message}"
            }
        }
    }

    fun joinChatRoom(matchId: String, userId: String, isAI: Boolean) {
        currentMatchId = matchId
        currentUserId = userId
        isAIConversation = isAI

        socketService?.let { service ->
            service.onChatHistory { history ->
                android.util.Log.d("ChatViewModel", "Received chat history: ${history.size} messages")
                val currentMsgs = _messages.value
                val mergedMsgs = mergeMessages(currentMsgs, history)
                _messages.value = mergedMsgs
            }

            service.onReceiveMessage { message ->
                android.util.Log.d("ChatViewModel", "Received new message: senderId=${message.senderId}, isFromAI=${AIConstants.isMessageFromAI(message.senderId)}")
                addMessage(message)
            }

            service.joinRoom(matchId, userId)
            _isSocketConnected.value = service.isConnected()

            android.util.Log.d("ChatViewModel", "Joined chat room: matchId=$matchId, userId=$userId, isAI=$isAI, connected=${service.isConnected()}")
        } ?: run {
            android.util.Log.e("ChatViewModel", "Socket service not initialized")
            _error.value = "Socket service not initialized"
        }
    }

    fun sendMessageViaSocket(text: String) {
        if (text.isBlank()) return

        android.util.Log.d("ChatViewModel", "sendMessageViaSocket: text=$text, matchId=$currentMatchId, senderId=$currentUserId, isAI=$isAIConversation, connected=${_isSocketConnected.value}")

        if (!_isSocketConnected.value) {
            android.util.Log.w("ChatViewModel", "Socket not connected, retrying...")
            _error.value = "Socket not connected. Đang kết nối..."
            socketService?.let {
                if (!it.isConnected()) {
                    it.connect()
                }
            }
            return
        }

        if (isAIConversation) {
            showAITypingIndicator()
        }

        socketService?.sendMessage(
            matchId = currentMatchId,
            senderId = currentUserId,
            message = text,
        ) ?: run {
            android.util.Log.e("ChatViewModel", "Socket service is null")
            _error.value = "Socket service not available"
        }
    }

    fun sendAudioViaSocket(audioPath: String, duration: Int) {
        if (!_isSocketConnected.value) {
            _error.value = "Socket not connected"
            return
        }

        socketService?.sendMessage(
            matchId = currentMatchId,
            senderId = currentUserId,
            audioPath = audioPath,
            duration = duration,
        )
    }

    fun sendImageViaSocket(imagePath: String) {
        if (!_isSocketConnected.value) {
            _error.value = "Socket not connected"
            return
        }

        socketService?.sendMessage(
            matchId = currentMatchId,
            senderId = currentUserId,
            imgChat = imagePath,
        )
    }

    // Typing indicator management
    fun showAITypingIndicator() {
        if (!isAIConversation) return

        _isAITyping.value = true

        // Auto hide after timeout
        typingTimeoutRunnable?.let { typingTimeoutHandler.removeCallbacks(it) }
        typingTimeoutRunnable = Runnable {
            hideAITypingIndicator()
        }
        typingTimeoutHandler.postDelayed(
            typingTimeoutRunnable!!,
            com.intern001.dating.presentation.ui.chat.AIConstants.AI_TYPING_TIMEOUT_MS,
        )
    }

    fun hideAITypingIndicator() {
        _isAITyping.value = false
        typingTimeoutRunnable?.let { typingTimeoutHandler.removeCallbacks(it) }
        typingTimeoutRunnable = null
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        typingTimeoutRunnable?.let { typingTimeoutHandler.removeCallbacks(it) }
        socketService?.disconnect()
        socketService = null
    }
}
