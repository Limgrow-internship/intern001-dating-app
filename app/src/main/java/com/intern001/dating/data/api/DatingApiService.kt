package com.intern001.dating.data.api

import com.intern001.dating.data.model.CountryResponse
import com.intern001.dating.data.model.LanguageResponse
import com.intern001.dating.data.model.request.LoginRequest
import com.intern001.dating.data.model.request.RequestOtpRequest
import com.intern001.dating.data.model.request.SignupRequest
import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.data.model.request.VerifyOtpRequest
import com.intern001.dating.data.model.response.AuthResponse
import com.intern001.dating.data.model.response.OtpResponse
import com.intern001.dating.data.model.response.RefreshTokenRequest
import com.intern001.dating.data.model.response.UserData
import com.intern001.dating.data.model.response.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface DatingApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/user/profile")
    suspend fun getCurrentUserProfile(): Response<UserData>

    @PUT("api/user/profile")
    suspend fun updateUserProfile(@Body request: UpdateProfileRequest): Response<UserData>

    @POST("api/user/request-otp")
    suspend fun requestOtp(@Body request: RequestOtpRequest): Response<OtpResponse>

    @POST("api/user/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @GET("all")
    suspend fun getAllCountries(
        @Query("fields") fields: String = "name,languages",
    ): List<CountryResponse>

    @GET("languages")
    suspend fun getLanguages(): List<LanguageResponse>

    @DELETE("api/user/account")
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
    ): Response<Unit>
}
