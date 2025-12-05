package com.intern001.dating.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.intern001.dating.data.local.dao.MessageDao
import com.intern001.dating.data.local.entity.MessageEntity

/**
 * Room Database để lưu trữ messages locally
 * Version 1 - có thể migrate sau này nếu cần thay đổi schema
 */
@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        const val DATABASE_NAME = "chat_database"
    }
}
