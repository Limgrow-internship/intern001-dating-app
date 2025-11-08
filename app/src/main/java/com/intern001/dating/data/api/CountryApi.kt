package com.intern001.dating.data.api

import com.intern001.dating.data.model.CountryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CountryApi {
    @GET("all")
    suspend fun getAllCountries(
        @Query("fields") fields: String = "name,languages",
    ): List<CountryResponse>
}
