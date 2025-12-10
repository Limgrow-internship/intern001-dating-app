package com.intern001.dating.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.intern001.dating.data.local.dao.MessageDao
import com.intern001.dating.data.local.entity.MessageEntity

@Database(
    entities = [MessageEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        const val DATABASE_NAME = "chat_database"
    }
}
