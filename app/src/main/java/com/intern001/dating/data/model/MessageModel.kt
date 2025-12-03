package com.intern001.dating.data.model

data class MessageModel(
    val senderId: String,
    val matchId: String,
    val message: String,
    val timestamp: String? = null,
    val audioPath: String? = null,
    val imgChat: String? = null,
    val duration: Int? = null,
)
