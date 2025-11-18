package com.intern001.dating.data.api

import com.intern001.dating.data.model.CountryResponse
import com.intern001.dating.data.model.LanguageResponse
import com.intern001.dating.data.model.request.BlockUserRequest
import com.intern001.dating.data.model.request.ChangePasswordRequest
import com.intern001.dating.data.model.request.FacebookLoginRequest
import com.intern001.dating.data.model.request.GoogleLoginRequest
import com.intern001.dating.data.model.request.LoginRequest
import com.intern001.dating.data.model.request.MatchActionRequest
import com.intern001.dating.data.model.request.RecommendationCriteriaRequest
import com.intern001.dating.data.model.request.RequestOtpRequest
import com.intern001.dating.data.model.request.SignupRequest
import com.intern001.dating.data.model.request.UnmatchRequest
import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.data.model.request.VerifyOtpRequest
import com.intern001.dating.data.model.response.AuthResponse
import com.intern001.dating.data.model.response.ChangePasswordResponse
import com.intern001.dating.data.model.response.FacebookLoginResponse
import com.intern001.dating.data.model.response.GoogleLoginResponse
import com.intern001.dating.data.model.response.MatchCardResponse
import com.intern001.dating.data.model.response.MatchCardsListResponse
import com.intern001.dating.data.model.response.MatchResponse
import com.intern001.dating.data.model.response.MatchResultResponse
import com.intern001.dating.data.model.response.MatchesListResponse
import com.intern001.dating.data.model.response.OtpResponse
import com.intern001.dating.data.model.response.RecommendationCriteriaResponse
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

    @POST("api/auth/google-login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): GoogleLoginResponse

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/profile")
    suspend fun getCurrentUserProfile(): Response<UserData>

    @PUT("api/profile")
    suspend fun updateUserProfile(@Body request: UpdateProfileRequest): Response<UserData>

    @POST("api/user/request-otp")
    suspend fun requestOtp(@Body request: RequestOtpRequest): Response<OtpResponse>

    @POST("api/user/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @PUT("api/user/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ChangePasswordResponse>

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

    @POST("api/auth/facebook-login")
    suspend fun facebookLogin(@Body request: FacebookLoginRequest): FacebookLoginResponse

    // Match endpoints
    @GET("api/matches/next")
    suspend fun getNextMatchCard(): Response<MatchCardResponse>

    @GET("api/matches/cards")
    suspend fun getMatchCards(
        @Query("limit") limit: Int = 10,
    ): Response<MatchCardsListResponse>

    @POST("api/matches/like")
    suspend fun likeUser(@Body request: MatchActionRequest): Response<MatchResultResponse>

    @POST("api/matches/pass")
    suspend fun passUser(@Body request: MatchActionRequest): Response<Unit>

    @POST("api/matches/superlike")
    suspend fun superLikeUser(@Body request: MatchActionRequest): Response<MatchResultResponse>

    @POST("api/matches/block")
    suspend fun blockUser(@Body request: BlockUserRequest): Response<Unit>

    @GET("api/matches")
    suspend fun getMatches(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): Response<MatchesListResponse>

    @GET("api/matches/{matchId}")
    suspend fun getMatchById(
        @Query("matchId") matchId: String,
    ): Response<MatchResponse>

    @POST("api/matches/unmatch")
    suspend fun unmatch(@Body request: UnmatchRequest): Response<Unit>

    // Recommendation endpoints
    @GET("api/recommendations/criteria")
    suspend fun getRecommendationCriteria(): Response<RecommendationCriteriaResponse>

    @PUT("api/recommendations/criteria")
    suspend fun updateRecommendationCriteria(
        @Body request: RecommendationCriteriaRequest,
    ): Response<RecommendationCriteriaResponse>
}
