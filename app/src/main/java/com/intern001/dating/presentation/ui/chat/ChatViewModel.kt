package com.intern001.dating.presentation.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(private val repo: ChatRepository) : ViewModel() {
    private val _messages = MutableLiveData<List<MessageModel>>(emptyList())
    val messages: LiveData<List<MessageModel>> get() = _messages

    fun fetchHistory(roomId: String) {
        viewModelScope.launch {
            try {
                _messages.postValue(repo.getHistory(roomId))
            } catch (_: Exception) { }
        }
    }

    fun sendMessage(message: MessageModel) {
        viewModelScope.launch {
            try {
                repo.sendMessage(message)
                fetchHistory(message.roomId)
            } catch (_: Exception) { }
        }
    }
}
