package com.intern001.dating.data.model.request

import com.google.gson.annotations.SerializedName

data class GenerateBioDto(
    @SerializedName("prompt")
    val prompt: String,
)
