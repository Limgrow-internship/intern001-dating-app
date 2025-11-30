package com.intern001.dating.data.model.response

data class UploadAudioResponse(
    val url: String,
    val public_id: String,
    val duration: Float?,
    val resource_type: String,
)
