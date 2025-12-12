package com.intern001.dating.data.model.response

import com.google.gson.annotations.SerializedName

data class EnhanceBioResponse(
    @SerializedName("originalBio")
    val originalBio: String?,

    @SerializedName("enhancedBio")
    val enhancedBio: String,

    @SerializedName("provider")
    val provider: String?,
)
