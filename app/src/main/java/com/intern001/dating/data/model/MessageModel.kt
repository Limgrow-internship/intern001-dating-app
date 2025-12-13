package com.intern001.dating.data.model

data class MessageModel(
    val id: String? = null,
    val clientMessageId: String? = null,
    val senderId: String,
    val matchId: String,
    val message: String,
    val timestamp: String? = null,
    val audioPath: String? = null,
    val imgChat: String? = null,
    val duration: Int? = null,
    val delivered: Boolean? = null,
    val replyToMessageId: String? = null,
    val replyToClientMessageId: String? = null,
    val replyToTimestamp: String? = null,
    val replyPreview: String? = null,
    val replySenderId: String? = null,
    val replySenderName: String? = null,
    val reaction: String? = null,
    val isReactionMessage: Boolean? = null,
)
