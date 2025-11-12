package com.intern001.dating.data.model

data class CountryResponse(
    val languages: List<LanguageApiModel>?,
)

data class LanguageApiModel(
    val iso639_1: String?,
    val name: String?,
    val nativeName: String?,
)
