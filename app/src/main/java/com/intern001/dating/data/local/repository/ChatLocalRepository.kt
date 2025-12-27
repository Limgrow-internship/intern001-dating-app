package com.intern001.dating.data.local.repository

import com.intern001.dating.common.performance.PerformanceMonitor
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
    companion object {
        private const val DEFAULT_PAGE_SIZE = 50
    }

    fun getMessagesByMatchId(matchId: String): Flow<List<MessageModel>> {
        return messageDao.getMessagesByMatchId(matchId).map { entities ->
            entities.map { it.toMessageModel() }
        }
    }

    suspend fun getMessagesByMatchIdSync(matchId: String): List<MessageModel> {
        return messageDao.getMessagesByMatchIdSync(matchId).map { it.toMessageModel() }
    }

    suspend fun getMessagesByMatchIdPaginated(
        matchId: String,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0,
    ): List<MessageModel> {
        return messageDao.getMessagesByMatchIdPaginated(matchId, limit, offset)
            .map { it.toMessageModel() }
    }

    suspend fun getRecentMessagesByMatchId(
        matchId: String,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): List<MessageModel> {
        return messageDao.getRecentMessagesByMatchId(matchId, limit)
            .map { it.toMessageModel() }
            .reversed() // Reverse to get ascending order
    }

    suspend fun saveMessage(message: MessageModel) {
        messageDao.insertMessage(message.toMessageEntity())
    }

    suspend fun saveMessages(messages: List<MessageModel>) {
        if (messages.isNotEmpty()) {
            PerformanceMonitor.measure("ChatLocalRepository.saveMessages") {
                // Use transaction for better performance with batch inserts
                val entities = PerformanceMonitor.measure("ChatLocalRepository.saveMessages.mapToEntity") {
                    messages.map { it.toMessageEntity() }
                }
                if (entities.size > 10) {
                    // Use transaction for large batches
                    PerformanceMonitor.measure("ChatLocalRepository.saveMessages.transaction") {
                        messageDao.insertMessagesInTransaction(entities)
                    }
                } else {
                    PerformanceMonitor.measure("ChatLocalRepository.saveMessages.insert") {
                        messageDao.insertMessages(entities)
                    }
                }
            }
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
        id = serverId ?: id,
        clientMessageId = clientMessageId,
        senderId = senderId,
        matchId = matchId,
        message = message ?: "",
        imgChat = imgChat,
        audioPath = audioPath,
        duration = duration,
        timestamp = timestamp,
        delivered = delivered,
        replyToMessageId = replyToMessageId,
        replyToClientMessageId = replyToClientMessageId,
        replyToTimestamp = replyToTimestamp,
        replyPreview = replyPreview,
        replySenderId = replySenderId,
        replySenderName = replySenderName,
        reaction = reaction,
    )
}

private fun MessageModel.toMessageEntity(): MessageEntity {
    val resolvedClientId = clientMessageId
    val resolvedServerId = id
    val pk =
        resolvedServerId
            ?: resolvedClientId
            ?: "${senderId}_${message ?: ""}_${matchId}_${imgChat ?: ""}_${audioPath ?: ""}_${timestamp ?: ""}"
                .hashCode()
                .toString()

    return MessageEntity(
        id = pk,
        serverId = resolvedServerId,
        clientMessageId = resolvedClientId,
        senderId = senderId,
        matchId = matchId,
        message = message,
        imgChat = imgChat,
        audioPath = audioPath,
        duration = duration,
        timestamp = timestamp,
        delivered = delivered,
        replyToMessageId = replyToMessageId,
        replyToClientMessageId = replyToClientMessageId,
        replyToTimestamp = replyToTimestamp,
        replyPreview = replyPreview,
        replySenderId = replySenderId,
        replySenderName = replySenderName,
        reaction = reaction,
    )
}
