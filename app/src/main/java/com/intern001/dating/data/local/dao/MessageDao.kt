package com.intern001.dating.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intern001.dating.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE matchId = :matchId ORDER BY timestamp ASC")
    fun getMessagesByMatchId(matchId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE matchId = :matchId ORDER BY timestamp ASC")
    suspend fun getMessagesByMatchIdSync(matchId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE matchId = :matchId ORDER BY timestamp ASC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesByMatchIdPaginated(matchId: String, limit: Int, offset: Int): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE matchId = :matchId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessagesByMatchId(matchId: String, limit: Int): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @androidx.room.Transaction
    suspend fun insertMessagesInTransaction(messages: List<MessageEntity>) {
        if (messages.isNotEmpty()) {
            insertMessages(messages)
        }
    }

    @Query("DELETE FROM messages WHERE matchId = :matchId")
    suspend fun deleteMessagesByMatchId(matchId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("SELECT COUNT(*) FROM messages WHERE matchId = :matchId")
    suspend fun getMessageCount(matchId: String): Int
}
