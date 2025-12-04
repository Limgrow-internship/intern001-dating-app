package com.intern001.dating.data.model.response

import com.google.gson.annotations.SerializedName

data class GenerateBioResponse(
    @SerializedName("generatedBio")
    val generatedBio: String,

    @SerializedName("provider")
    val provider: String,
)
