package com.intern001.dating.data.model

data class MessageModel(
    val senderId: String,
    val roomId: String,
    val message: String,
    val timestamp: String? = null,
)
