package com.intern001.dating.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity để lưu messages vào Room Database
 * Hỗ trợ offline access và persist data
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String, // Unique ID được tạo từ senderId + message + matchId + timestamp
    val senderId: String,
    val matchId: String,
    val message: String?,
    val imgChat: String?,
    val audioPath: String?,
    val duration: Int?,
    val timestamp: String?,
    val delivered: Boolean?,
    val createdAt: Long = System.currentTimeMillis(), // Thời gian lưu vào database
)
