package com.intern001.dating.presentation.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.model.MessageModel
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
) : ViewModel() {
    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages

    private val _matchStatus = MutableStateFlow("active")
    val matchStatus: StateFlow<String> = _matchStatus

    fun fetchHistory(matchId: String) {
        viewModelScope.launch {
            try {
                _messages.value = repo.getHistory(matchId)
            } catch (_: Exception) { }
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

    fun fetchMatchStatus(targetUserId: String) {
        viewModelScope.launch {
            try {
                val status = repo.getMatchStatus(targetUserId)
                _matchStatus.value = status
            } catch (e: Exception) {
                _matchStatus.value = "error"
            }
        }
    }

    /**
     * Thêm message mới vào danh sách (dùng cho real-time socket messages)
     */
    fun addMessage(message: MessageModel) {
        val currentMessages = _messages.value
        // Kiểm tra xem message đã tồn tại chưa để tránh duplicate
        if (!currentMessages.any { 
            it.senderId == message.senderId && 
            it.message == message.message && 
            it.matchId == message.matchId &&
            it.imgChat == message.imgChat &&
            it.audioPath == message.audioPath
        }) {
            _messages.value = currentMessages + message
        }
    }
}
