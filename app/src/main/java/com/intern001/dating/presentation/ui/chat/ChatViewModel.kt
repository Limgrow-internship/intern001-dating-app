package com.intern001.dating.presentation.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository,
) : ViewModel() {
    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages

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
}
