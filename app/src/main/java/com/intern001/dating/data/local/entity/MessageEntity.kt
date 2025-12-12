package com.intern001.dating.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val serverId: String? = null,
    val clientMessageId: String?,
    val senderId: String,
    val matchId: String,
    val message: String?,
    val imgChat: String?,
    val audioPath: String?,
    val duration: Int?,
    val timestamp: String?,
    val delivered: Boolean?,
    val replyToMessageId: String? = null,
    val replyToClientMessageId: String? = null,
    val replyToTimestamp: String? = null,
    val replyPreview: String? = null,
    val replySenderId: String? = null,
    val replySenderName: String? = null,
    val reaction: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
