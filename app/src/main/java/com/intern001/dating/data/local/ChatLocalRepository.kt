package com.intern001.dating.data.local

import com.intern001.dating.data.local.dao.MessageDao
import com.intern001.dating.data.local.entity.MessageEntity
import com.intern001.dating.data.model.MessageModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ChatLocalRepository @Inject constructor(
    private val messageDao: MessageDao,
) {
    fun getMessagesByMatchId(matchId: String): Flow<List<MessageModel>> {
        return messageDao.getMessagesByMatchId(matchId).map { entities ->
            entities.map { it.toMessageModel() }
        }
    }

    suspend fun getMessagesByMatchIdSync(matchId: String): List<MessageModel> {
        return messageDao.getMessagesByMatchIdSync(matchId).map { it.toMessageModel() }
    }

    suspend fun saveMessage(message: MessageModel) {
        messageDao.insertMessage(message.toMessageEntity())
    }

    suspend fun saveMessages(messages: List<MessageModel>) {
        if (messages.isNotEmpty()) {
            messageDao.insertMessages(messages.map { it.toMessageEntity() })
        }
    }

    suspend fun deleteMessagesByMatchId(matchId: String) {
        messageDao.deleteMessagesByMatchId(matchId)
    }

    suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    suspend fun getMessageCount(matchId: String): Int {
        return messageDao.getMessageCount(matchId)
    }
}

private fun MessageEntity.toMessageModel(): MessageModel {
    return MessageModel(
        clientMessageId = clientMessageId,
        senderId = senderId,
        matchId = matchId,
        message = message ?: "",
        imgChat = imgChat,
        audioPath = audioPath,
        duration = duration,
        timestamp = timestamp,
        delivered = delivered,
    )
}

private fun MessageModel.toMessageEntity(): MessageEntity {
    val resolvedClientId = clientMessageId
    val id =
        resolvedClientId
            ?: "${senderId}_${message ?: ""}_${matchId}_${imgChat ?: ""}_${audioPath ?: ""}_${timestamp ?: ""}"
                .hashCode()
                .toString()

    return MessageEntity(
        id = id,
        clientMessageId = resolvedClientId,
        senderId = senderId,
        matchId = matchId,
        message = message,
        imgChat = imgChat,
        audioPath = audioPath,
        duration = duration,
        timestamp = timestamp,
        delivered = delivered,
    )
}
