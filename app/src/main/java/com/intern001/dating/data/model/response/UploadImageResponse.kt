package com.intern001.dating.data.model.response

data class UploadImageResponse(
    val url: CloudinaryUrl,
)
data class CloudinaryUrl(
    val secure_url: String,
)
