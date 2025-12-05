package com.intern001.dating.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intern001.dating.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO để query messages từ Room Database
 */
@Dao
interface MessageDao {
    /**
     * Lấy tất cả messages của một matchId, sorted theo timestamp
     * Flow để tự động update khi có thay đổi
     */
    @Query("SELECT * FROM messages WHERE matchId = :matchId ORDER BY timestamp ASC")
    fun getMessagesByMatchId(matchId: String): Flow<List<MessageEntity>>

    /**
     * Lấy messages sync (không phải Flow) - dùng khi cần load ngay
     */
    @Query("SELECT * FROM messages WHERE matchId = :matchId ORDER BY timestamp ASC")
    suspend fun getMessagesByMatchIdSync(matchId: String): List<MessageEntity>

    /**
     * Insert một message vào database
     * REPLACE nếu đã tồn tại (dựa vào id)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    /**
     * Insert nhiều messages cùng lúc
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    /**
     * Xóa tất cả messages của một matchId
     */
    @Query("DELETE FROM messages WHERE matchId = :matchId")
    suspend fun deleteMessagesByMatchId(matchId: String)

    /**
     * Xóa tất cả messages trong database
     */
    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    /**
     * Đếm số lượng messages của một matchId
     */
    @Query("SELECT COUNT(*) FROM messages WHERE matchId = :matchId")
    suspend fun getMessageCount(matchId: String): Int
}
