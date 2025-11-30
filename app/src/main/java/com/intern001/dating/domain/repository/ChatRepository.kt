package com.intern001.dating.domain.repository

import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.domain.entity.LastMessageEntity

interface ChatRepository {
    suspend fun sendMessage(message: MessageModel): MessageModel
    suspend fun getHistory(matchId: String): List<MessageModel>

    suspend fun getLastMessage(matchId: String): LastMessageEntity
    suspend fun uploadAudio(localPath: String): String?
}
