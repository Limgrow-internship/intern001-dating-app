package com.intern001.dating.data.model.response

import com.google.gson.annotations.SerializedName

data class GenerateBioResponse(
    @SerializedName("bio")
    val bio: String,

    @SerializedName("message")
    val message: String,
)
