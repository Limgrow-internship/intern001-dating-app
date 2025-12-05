package com.intern001.dating.data.local

import com.intern001.dating.data.local.dao.MessageDao
import com.intern001.dating.data.local.entity.MessageEntity
import com.intern001.dating.data.model.MessageModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository để quản lý local storage của messages
 * Cung cấp interface để save/load messages từ Room Database
 */
@Singleton
class ChatLocalRepository @Inject constructor(
    private val messageDao: MessageDao,
) {
    /**
     * Lấy messages theo matchId dưới dạng Flow
     * Tự động update khi có thay đổi trong database
     */
    fun getMessagesByMatchId(matchId: String): Flow<List<MessageModel>> {
        return messageDao.getMessagesByMatchId(matchId).map { entities ->
            entities.map { it.toMessageModel() }
        }
    }

    /**
     * Lấy messages sync (không phải Flow) - dùng khi cần load ngay
     * Hữu ích khi offline hoặc cần load nhanh từ cache
     */
    suspend fun getMessagesByMatchIdSync(matchId: String): List<MessageModel> {
        return messageDao.getMessagesByMatchIdSync(matchId).map { it.toMessageModel() }
    }

    /**
     * Lưu một message vào database
     */
    suspend fun saveMessage(message: MessageModel) {
        messageDao.insertMessage(message.toMessageEntity())
    }

    /**
     * Lưu nhiều messages cùng lúc
     * Dùng khi fetch history từ server
     */
    suspend fun saveMessages(messages: List<MessageModel>) {
        if (messages.isNotEmpty()) {
            messageDao.insertMessages(messages.map { it.toMessageEntity() })
        }
    }

    /**
     * Xóa tất cả messages của một matchId
     */
    suspend fun deleteMessagesByMatchId(matchId: String) {
        messageDao.deleteMessagesByMatchId(matchId)
    }

    /**
     * Xóa tất cả messages trong database
     */
    suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    /**
     * Đếm số lượng messages của một matchId
     */
    suspend fun getMessageCount(matchId: String): Int {
        return messageDao.getMessageCount(matchId)
    }
}

/**
 * Extension function để convert MessageEntity thành MessageModel
 */
private fun MessageEntity.toMessageModel(): MessageModel {
    return MessageModel(
        senderId = senderId,
        matchId = matchId,
        message = message ?: "", // Convert nullable String? thành non-nullable String
        imgChat = imgChat,
        audioPath = audioPath,
        duration = duration,
        timestamp = timestamp,
        delivered = delivered,
    )
}

/**
 * Extension function để convert MessageModel thành MessageEntity
 * Tạo unique ID từ các fields để tránh duplicate
 */
private fun MessageModel.toMessageEntity(): MessageEntity {
    // Tạo unique ID từ các fields quan trọng
    val id = "${senderId}_${message ?: ""}_${matchId}_${imgChat ?: ""}_${audioPath ?: ""}_${timestamp ?: ""}"
        .hashCode()
        .toString()

    return MessageEntity(
        id = id,
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
