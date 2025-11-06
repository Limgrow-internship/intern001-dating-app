package com.intern001.dating.data.api

import com.intern001.dating.data.model.request.LoginRequest
import com.intern001.dating.data.model.request.RequestOtpRequest
import com.intern001.dating.data.model.request.SignupRequest
import com.intern001.dating.data.model.request.VerifyOtpRequest
import com.intern001.dating.data.model.response.AuthResponse
import com.intern001.dating.data.model.response.OtpResponse
import com.intern001.dating.data.model.response.RefreshTokenRequest
import com.intern001.dating.data.model.response.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DatingApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("user/request-otp")
    suspend fun requestOtp(@Body request: RequestOtpRequest): Response<OtpResponse>

    @POST("user/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>
}
