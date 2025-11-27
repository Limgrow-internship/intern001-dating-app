package com.intern001.dating.data.repository

import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.data.model.response.toEntity
import com.intern001.dating.domain.entity.LastMessageEntity
import com.intern001.dating.domain.repository.ChatRepository
import javax.inject.Inject
import javax.inject.Named

class ChatRepositoryImpl @Inject constructor(
    @Named("datingApi") private val api: DatingApiService,
) : ChatRepository {
    override suspend fun sendMessage(message: MessageModel): MessageModel = api.sendMessage(message)

    override suspend fun getHistory(matchId: String): List<MessageModel> = api.getHistory(matchId)

    override suspend fun getLastMessage(matchId: String): LastMessageEntity {
        val resp = api.getLastMessage(matchId)
        return resp.toEntity()
    }
}
