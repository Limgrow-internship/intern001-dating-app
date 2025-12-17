package com.intern001.dating.presentation.ui.chat

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.common.performance.PerformanceMonitor
import com.intern001.dating.data.local.prefs.TokenManager
import com.intern001.dating.data.local.repository.ChatLocalRepository
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.data.service.ChatSocketService
import com.intern001.dating.domain.repository.ChatRepository
import com.intern001.dating.domain.usecase.SendVoiceMessageUseCase
import com.intern001.dating.presentation.util.AIConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.util.ArrayDeque
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository,
    private val sendVoiceMessageUseCase: SendVoiceMessageUseCase,
    private val tokenManager: TokenManager,
    private val localRepo: ChatLocalRepository,
) : ViewModel() {
    private var lastSentClientMessageId: String? = null
    private var lastSentAtMs: Long = 0L
    private val pendingClientMessageIds: ArrayDeque<String> = ArrayDeque()

    // Map to store pending reactions: targetMessageId -> reaction
    private val pendingReactions: MutableMap<String, String> = mutableMapOf()

    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages
    private var cachedSortedMessages: List<MessageModel>? = null
    private var lastSortTimestamp: String? = null

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
                val sorted = allMsgs.sortedBy { it.timestamp ?: "" }
                _messages.value = sorted
                cachedSortedMessages = sorted
                lastSortTimestamp = sorted.lastOrNull()?.timestamp

                try {
                    localRepo.saveMessages(allMsgs)
                    android.util.Log.d("ChatViewModel", "Saved ${allMsgs.size} messages to local DB")
                } catch (e: Exception) {
                    android.util.Log.e("ChatViewModel", "Failed to save messages to local DB", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Failed to fetch history", e)
                try {
                    val localMessages = localRepo.getMessagesByMatchIdSync(matchId)
                    if (localMessages.isNotEmpty()) {
                        android.util.Log.d("ChatViewModel", "Loaded ${localMessages.size} messages from local DB (offline)")
                        val sorted = localMessages.sortedBy { it.timestamp ?: "" }
                        _messages.value = sorted
                        cachedSortedMessages = sorted
                        lastSortTimestamp = sorted.lastOrNull()?.timestamp
                    }
                } catch (localError: Exception) {
                    android.util.Log.e("ChatViewModel", "Failed to load from local DB", localError)
                }
            }
        }
    }

    fun updateMessagesFromCache(cachedMessages: List<MessageModel>) {
        val currentMsgs = _messages.value
        val mergedMsgs = mergeMessages(currentMsgs, cachedMessages)
        _messages.value = mergedMsgs
        android.util.Log.d("ChatViewModel", "Updated messages from cache: ${mergedMsgs.size} messages")
    }

    private fun messageKey(msg: MessageModel): String {
        msg.id?.let { return "id_$it" }
        msg.clientMessageId?.let { return "cid_$it" }
        return "${msg.senderId}_${msg.message}_${msg.matchId}_${msg.imgChat}_${msg.audioPath}_${msg.timestamp ?: ""}"
    }

    private fun contentKey(msg: MessageModel): String {
        msg.id?.let { return "id_$it" }
        msg.clientMessageId?.let { return "cid_$it" }
        return "${msg.senderId}_${msg.message}_${msg.matchId}_${msg.imgChat}_${msg.audioPath}"
    }

    fun newClientMessageId(): String = UUID.randomUUID().toString()

    private fun mergeMessages(current: List<MessageModel>, new: List<MessageModel>): List<MessageModel> {
        return PerformanceMonitor.measure("ChatViewModel.mergeMessages") {
            // Use HashMap for O(1) lookup instead of O(n) find operations
            val merged = HashMap<String, MessageModel>(current.size + new.size)

            fun uniqueKey(msg: MessageModel): String {
                // Prefer clientMessageId; fallback to previous composite keys
                msg.clientMessageId?.let { return "cid_$it" }
                msg.id?.let { return "id_$it" }
                return if (!msg.timestamp.isNullOrBlank()) {
                    messageKey(msg)
                } else {
                    "${contentKey(msg)}_${msg.delivered ?: "unknown"}"
                }
            }

            // Build lookup map for existing messages
            val currentMap = current.associateBy { uniqueKey(it) }
            merged.putAll(currentMap)

            // Merge new messages efficiently
            new.forEach { msg ->
                val key = uniqueKey(msg)
                val existing = merged[key]
                if (existing != null && isSameEvent(existing, msg)) {
                    merged[key] = preferMessage(existing, msg)
                } else {
                    // Check if message exists with different key
                    val existingByEvent = merged.values.firstOrNull { isSameEvent(it, msg) }
                    if (existingByEvent != null) {
                        val existingKey = uniqueKey(existingByEvent)
                        merged[existingKey] = preferMessage(existingByEvent, msg)
                    } else {
                        merged[key] = msg
                    }
                }
            }

            // Only sort if necessary (when new messages added or order changed)
            val sorted = PerformanceMonitor.measure("ChatViewModel.mergeMessages.sort") {
                merged.values.sortedBy { it.timestamp ?: "" }
            }
            cachedSortedMessages = sorted
            lastSortTimestamp = sorted.lastOrNull()?.timestamp
            sorted
        }
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
            sendVoiceMessageUseCase(matchId, senderId, localAudioPath, duration, newClientMessageId())
            fetchHistory(matchId)
        }
    }

    fun sendImageMessage(imageUrl: String, matchId: String, senderId: String) {
        viewModelScope.launch {
            val message = MessageModel(
                clientMessageId = newClientMessageId(),
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

    fun clearMessagesForMyself(matchId: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                repo.clearMessagesForUser(matchId)
                localRepo.deleteMessagesByMatchId(matchId)
                fetchHistory(matchId)
                onDone?.invoke()
            } catch (_: Exception) {}
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
        PerformanceMonitor.measure("ChatViewModel.addMessage") {
            val currentMessages = _messages.value
            val needsSort = cachedSortedMessages == null ||
                (message.timestamp != null && message.timestamp != lastSortTimestamp)

            if (!message.reaction.isNullOrBlank()) {
                if (!message.replyToMessageId.isNullOrBlank()) {
                    val targetId = message.replyToMessageId
                    val targetIndex = currentMessages.indexOfFirst {
                        it.id == targetId || it.clientMessageId == targetId
                    }

                    if (targetIndex >= 0) {
                        val updated = currentMessages.toMutableList().apply {
                            set(targetIndex, this[targetIndex].copy(reaction = message.reaction))
                        }
                        _messages.value = if (needsSort) updated.sortedBy { it.timestamp ?: "" } else updated
                        if (needsSort) {
                            cachedSortedMessages = _messages.value
                            lastSortTimestamp = _messages.value.lastOrNull()?.timestamp
                        }
                        return
                    }
                }

                val existingIndex = currentMessages.indexOfFirst {
                    (!message.id.isNullOrBlank() && it.id == message.id) ||
                        (!message.clientMessageId.isNullOrBlank() && it.clientMessageId == message.clientMessageId)
                }

                if (existingIndex >= 0) {
                    val existing = currentMessages[existingIndex]
                    val updated = currentMessages.toMutableList().apply {
                        set(existingIndex, preferMessage(existing, message).copy(reaction = message.reaction))
                    }
                    _messages.value = if (needsSort) updated.sortedBy { it.timestamp ?: "" } else updated
                    if (needsSort) {
                        cachedSortedMessages = _messages.value
                        lastSortTimestamp = _messages.value.lastOrNull()?.timestamp
                    }
                    return
                }
            }

            val hasNoContent = message.message.isNullOrBlank() &&
                message.imgChat.isNullOrBlank() &&
                message.audioPath.isNullOrBlank()

            if (hasNoContent && !message.replyToMessageId.isNullOrBlank()) {
                return
            }

            val existingIndex = currentMessages.indexOfFirst {
                (!message.id.isNullOrBlank() && it.id == message.id) ||
                    (!message.clientMessageId.isNullOrBlank() && it.clientMessageId == message.clientMessageId)
            }

            if (existingIndex >= 0) {
                val existing = currentMessages[existingIndex]
                val updated = currentMessages.toMutableList().apply {
                    set(existingIndex, preferMessage(existing, message))
                }
                _messages.value = if (needsSort) updated.sortedBy { it.timestamp ?: "" } else updated
                if (needsSort) {
                    cachedSortedMessages = _messages.value
                    lastSortTimestamp = _messages.value.lastOrNull()?.timestamp
                }
                return
            }

            val existingEventIndex = currentMessages.indexOfFirst { isSameEvent(it, message) }
            if (existingEventIndex >= 0) {
                val existing = currentMessages[existingEventIndex]
                val preferred = preferMessage(existing, message)
                if (preferred == existing) {
                    return
                }
                val updated = currentMessages.toMutableList().apply {
                    set(existingEventIndex, preferred)
                }
                _messages.value = if (needsSort) updated.sortedBy { it.timestamp ?: "" } else updated
                if (needsSort) {
                    cachedSortedMessages = _messages.value
                    lastSortTimestamp = _messages.value.lastOrNull()?.timestamp
                }
                return
            }

            val updated = currentMessages + message
            _messages.value = if (needsSort) updated.sortedBy { it.timestamp ?: "" } else updated
            if (needsSort) {
                cachedSortedMessages = _messages.value
                lastSortTimestamp = _messages.value.lastOrNull()?.timestamp
            }

            if (isAIConversation && AIConstants.isMessageFromAI(message.senderId)) {
                hideAITypingIndicator()
            }
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
        pendingClientMessageIds.clear()

        socketService?.let { service ->
            service.onChatHistory { history ->
                viewModelScope.launch {
                    android.util.Log.d("ChatViewModel", "Received chat history: ${history.size} messages")
                    val currentMsgs = _messages.value
                    val mergedMsgs = mergeMessages(currentMsgs, history)
                    _messages.value = mergedMsgs
                }
            }

            service.onReceiveMessage { message ->
                viewModelScope.launch {
                    val isOwnMessage = message.senderId == currentUserId

                    val normalizedMessage = if (isOwnMessage) {
                        when {
                            !message.clientMessageId.isNullOrBlank() -> {
                                pendingClientMessageIds.remove(message.clientMessageId)
                                message
                            }
                            pendingClientMessageIds.isNotEmpty() -> {
                                val mappedId = pendingClientMessageIds.removeFirst()
                                message.copy(clientMessageId = mappedId)
                            }
                            else -> message
                        }
                    } else {
                        message
                    }

                    addMessage(normalizedMessage)
                }
            }

            service.joinRoom(matchId, userId)
            _isSocketConnected.value = service.isConnected()
        } ?: run {
            android.util.Log.e("ChatViewModel", "Socket service not initialized")
            _error.value = "Socket service not initialized"
        }
    }

    fun sendMessageViaSocket(
        text: String,
        clientMessageId: String? = null,
        replyToMessageId: String? = null,
        replyToClientMessageId: String? = null,
        replyToTimestamp: String? = null,
        replyPreview: String? = null,
        replySenderId: String? = null,
        replySenderName: String? = null,
        reaction: String? = null,
    ) {
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

        val finalClientId = clientMessageId ?: newClientMessageId()
        lastSentClientMessageId = finalClientId
        lastSentAtMs = System.currentTimeMillis()
        pendingClientMessageIds.addLast(finalClientId)

        socketService?.sendMessage(
            matchId = currentMatchId,
            senderId = currentUserId,
            message = text,
            clientMessageId = finalClientId,
            replyToMessageId = replyToMessageId,
            replyToClientMessageId = replyToClientMessageId,
            replyToTimestamp = replyToTimestamp,
            replyPreview = replyPreview,
            replySenderId = replySenderId,
            replySenderName = replySenderName,
            reaction = reaction,
        ) ?: run {
            android.util.Log.e("ChatViewModel", "Socket service is null")
            _error.value = "Socket service not available"
        }
    }

    fun sendAudioViaSocket(audioPath: String, duration: Int, clientMessageId: String? = null) {
        if (!_isSocketConnected.value) {
            _error.value = "Socket not connected"
            return
        }

        val finalClientId = clientMessageId ?: newClientMessageId()
        pendingClientMessageIds.addLast(finalClientId)

        socketService?.sendMessage(
            matchId = currentMatchId,
            senderId = currentUserId,
            audioPath = audioPath,
            duration = duration,
            clientMessageId = finalClientId,
        )
    }

    fun sendImageViaSocket(imagePath: String, clientMessageId: String? = null) {
        if (!_isSocketConnected.value) {
            _error.value = "Socket not connected"
            return
        }

        val finalClientId = clientMessageId ?: newClientMessageId()
        pendingClientMessageIds.addLast(finalClientId)

        socketService?.sendMessage(
            matchId = currentMatchId,
            senderId = currentUserId,
            imgChat = imagePath,
            clientMessageId = finalClientId,
        )
    }

    fun showAITypingIndicator() {
        if (!isAIConversation) return

        _isAITyping.value = true

        typingTimeoutRunnable?.let { typingTimeoutHandler.removeCallbacks(it) }
        typingTimeoutRunnable = Runnable {
            hideAITypingIndicator()
        }
        typingTimeoutHandler.postDelayed(
            typingTimeoutRunnable!!,
            AIConstants.AI_TYPING_TIMEOUT_MS,
        )
    }

    fun hideAITypingIndicator() {
        _isAITyping.value = false
        typingTimeoutRunnable?.let { typingTimeoutHandler.removeCallbacks(it) }
        typingTimeoutRunnable = null
    }

    private fun preferMessage(existing: MessageModel, incoming: MessageModel): MessageModel {
        val incomingHasTimestamp = !incoming.timestamp.isNullOrBlank()
        val existingHasTimestamp = !existing.timestamp.isNullOrBlank()
        val incomingDelivered = incoming.delivered == true
        val existingDelivered = existing.delivered == true

        val candidate = when {
            existingDelivered && !incomingDelivered -> existing
            incomingDelivered && !existingDelivered -> incoming
            incomingHasTimestamp && !existingHasTimestamp -> incoming
            existingHasTimestamp && !incomingHasTimestamp -> existing
            else -> {
                val incTs = incoming.timestamp ?: ""
                val exTs = existing.timestamp ?: ""
                if (incTs > exTs) incoming else existing
            }
        }

        val other = if (candidate === existing) incoming else existing
        // For reaction, prefer the non-null one, and if both exist, prefer the incoming one (newer)
        val preferredReaction = when {
            !incoming.reaction.isNullOrBlank() -> incoming.reaction
            !existing.reaction.isNullOrBlank() -> existing.reaction
            else -> null
        }
        return candidate.copy(
            replyToMessageId = candidate.replyToMessageId ?: other.replyToMessageId,
            replyPreview = candidate.replyPreview ?: other.replyPreview,
            replySenderId = candidate.replySenderId ?: other.replySenderId,
            replySenderName = candidate.replySenderName ?: other.replySenderName,
            reaction = preferredReaction,
        )
    }

    fun applyReaction(targetId: String?, reaction: String?) {
        if (targetId.isNullOrBlank()) {
            android.util.Log.w("ChatViewModel", "applyReaction: targetId is null or blank")
            return
        }
        val currentMessages = _messages.value
        val targetIndex = currentMessages.indexOfFirst {
            it.id == targetId || it.clientMessageId == targetId
        }
        if (targetIndex < 0) {
            android.util.Log.w("ChatViewModel", "applyReaction: No message found with id=$targetId")
            return
        }
        val targetMessage = currentMessages[targetIndex]
        val updated = currentMessages.toMutableList().apply {
            set(targetIndex, targetMessage.copy(reaction = reaction))
        }
        _messages.value = updated

        targetMessage?.let { msg ->
            val targetMsgId = msg.id ?: targetId
            val targetClientId = msg.clientMessageId
            if (targetMsgId != null || targetClientId != null) {
                if (targetMsgId != null) {
                    pendingReactions[targetMsgId] = reaction ?: ""
                }
                if (targetClientId != null) {
                    pendingReactions[targetClientId] = reaction ?: ""
                }

                if (_isSocketConnected.value) {
                    if (targetMsgId != null || targetClientId != null) {
                        socketService?.sendMessage(
                            matchId = currentMatchId,
                            senderId = currentUserId,
                            message = "",
                            clientMessageId = null,
                            replyToMessageId = targetMsgId,
                            replyToClientMessageId = targetClientId,
                            reaction = reaction,
                        )
                        android.util.Log.d("ChatViewModel", "Sent reaction to server: reaction=$reaction, targetId=$targetId, serverId=$targetMsgId, clientId=$targetClientId")
                    } else {
                        android.util.Log.e("ChatViewModel", "Cannot send reaction: both serverId and clientId are null")
                    }
                } else {
                    android.util.Log.w("ChatViewModel", "Socket not connected, reaction not sent to server")
                }
            }
        }
    }

    private fun parseMillis(ts: String?): Long? {
        return try {
            ts?.let { Instant.parse(it).toEpochMilli() }
        } catch (_: Exception) {
            null
        }
    }

    private fun isSameEvent(existing: MessageModel, incoming: MessageModel): Boolean {
        val existingCid = existing.clientMessageId
        val incomingCid = incoming.clientMessageId

        if (!existingCid.isNullOrBlank() || !incomingCid.isNullOrBlank()) {
            if (!existingCid.isNullOrBlank() && !incomingCid.isNullOrBlank()) {
                return existingCid == incomingCid
            }

            if (contentKey(existing) != contentKey(incoming)) return false
            val exMs = parseMillis(existing.timestamp)
            val inMs = parseMillis(incoming.timestamp)
            if (exMs != null && inMs != null) {
                val diff = abs(exMs - inMs)
                if (diff <= 5_000) return true
            }
            if (exMs == null || inMs == null) {
                return true
            }
            return false
        }

        if (contentKey(existing) != contentKey(incoming)) return false

        val exMs = parseMillis(existing.timestamp)
        val inMs = parseMillis(incoming.timestamp)

        if (exMs != null && inMs != null) {
            val diff = abs(exMs - inMs)
            if (diff <= 5_000) return true
        }

        if (exMs == null || inMs == null) {
            return true
        }

        return false
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
