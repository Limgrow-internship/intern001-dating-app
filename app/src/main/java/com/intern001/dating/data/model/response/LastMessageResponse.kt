package com.intern001.dating.data.model.response

import com.intern001.dating.domain.entity.LastMessageEntity

data class LastMessageResponse(
    val message: String,
    val senderId: String,
    val timestamp: String,
)
fun LastMessageResponse.toEntity() = LastMessageEntity(
    message = this.message,
    senderId = this.senderId,
    timestamp = this.timestamp,
)
