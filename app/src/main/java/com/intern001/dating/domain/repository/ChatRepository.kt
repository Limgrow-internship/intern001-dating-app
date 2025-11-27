package com.intern001.dating.domain.repository

import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.domain.entity.LastMessageEntity

interface ChatRepository {
    suspend fun sendMessage(message: MessageModel): MessageModel
    suspend fun getHistory(roomId: String): List<MessageModel>

    suspend fun getLastMessage(roomId: String): LastMessageEntity
}
