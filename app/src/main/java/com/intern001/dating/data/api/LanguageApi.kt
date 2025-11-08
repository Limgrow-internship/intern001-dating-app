package com.intern001.dating.data.api

import com.intern001.dating.data.model.LanguageResponse
import retrofit2.http.GET

interface LanguageApi {
    @GET("languages")
    suspend fun getLanguages(): List<LanguageResponse>
}
