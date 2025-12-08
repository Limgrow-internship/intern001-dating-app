package com.intern001.dating.data.api

import com.intern001.dating.data.model.CountryResponse
import com.intern001.dating.data.model.LanguageResponse
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.data.model.request.BlockUserRequest
import com.intern001.dating.data.model.request.ChangePasswordRequest
import com.intern001.dating.data.model.request.FacebookLoginRequest
import com.intern001.dating.data.model.request.GenerateBioDto
import com.intern001.dating.data.model.request.GoogleLoginRequest
import com.intern001.dating.data.model.request.LoginRequest
import com.intern001.dating.data.model.request.MatchActionRequest
import com.intern001.dating.data.model.request.RecommendationCriteriaRequest
import com.intern001.dating.data.model.request.ReorderPhotosRequest
import com.intern001.dating.data.model.request.ReportRequest
import com.intern001.dating.data.model.request.RequestOtpRequest
import com.intern001.dating.data.model.request.SignupRequest
import com.intern001.dating.data.model.request.UnmatchRequest
import com.intern001.dating.data.model.request.UnmatchUserRequest
import com.intern001.dating.data.model.request.UpdateFCMTokenRequest
import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.data.model.request.VerifyOtpRequest
import com.intern001.dating.data.model.response.AuthResponse
import com.intern001.dating.data.model.response.ChangePasswordResponse
import com.intern001.dating.data.model.response.FacebookLoginResponse
import com.intern001.dating.data.model.response.GenerateBioResponse
import com.intern001.dating.data.model.response.GoogleLoginResponse
import com.intern001.dating.data.model.response.LastMessageResponse
import com.intern001.dating.data.model.response.LikedYouResponseDto
import com.intern001.dating.data.model.response.MatchCardResponse
import com.intern001.dating.data.model.response.MatchCardsListResponse
import com.intern001.dating.data.model.response.MatchResponse
import com.intern001.dating.data.model.response.MatchResponseDTO
import com.intern001.dating.data.model.response.MatchResultResponse
import com.intern001.dating.data.model.response.MatchStatusResponse
import com.intern001.dating.data.model.response.MatchesListResponse
import com.intern001.dating.data.model.response.OtpResponse
import com.intern001.dating.data.model.response.PhotoCountResponse
import com.intern001.dating.data.model.response.PhotoListResponse
import com.intern001.dating.data.model.response.PhotoResponse
import com.intern001.dating.data.model.response.RecommendationCriteriaResponse
import com.intern001.dating.data.model.response.RefreshTokenRequest
import com.intern001.dating.data.model.response.ReportResponse
import com.intern001.dating.data.model.response.UploadAudioResponse
import com.intern001.dating.data.model.response.UploadImageResponse
import com.intern001.dating.data.model.response.UserData
import com.intern001.dating.data.model.response.VerifyFaceResponse
import com.intern001.dating.data.model.response.VerifyOtpResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
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

    @POST("api/report/create")
    suspend fun createReport(@Body body: ReportRequest): ReportResponse

    @GET("api/profile/{userId}")
    suspend fun getProfileByUserId(@Path("userId") userId: String): Response<MatchCardResponse>

    @PUT("api/profile")
    suspend fun updateUserProfile(@Body request: UpdateProfileRequest): Response<UserData>

    @POST("api/ai/generate-bio")
    suspend fun generateBio(@Body request: GenerateBioDto): Response<GenerateBioResponse>

    @POST("api/ai/enhance-bio")
    suspend fun enhanceBio(): Response<GenerateBioResponse>

    @DELETE("api/profile")
    suspend fun deleteProfile(): Response<Unit>

    // ============================================================
    // Photo Management APIs
    // ============================================================

    // Get all photos for current user
    @GET("api/photos")
    suspend fun getPhotos(): Response<PhotoListResponse>

    // Get primary photo (avatar)
    @GET("api/photos/primary")
    suspend fun getPrimaryPhoto(): Response<PhotoResponse>

    // Get photo count
    @GET("api/photos/count")
    suspend fun getPhotoCount(): Response<PhotoCountResponse>

    // Upload new photo
    @Multipart
    @POST("api/photos/upload")
    suspend fun uploadPhoto(
        @Part file: MultipartBody.Part,
        @Part type: MultipartBody.Part, // "avatar" | "gallery" | "selfie"
    ): Response<PhotoResponse>

    // Set photo as primary (avatar)
    @PUT("api/photos/{photoId}/set-primary")
    suspend fun setPhotoAsPrimary(@Path("photoId") photoId: String): Response<PhotoResponse>

    // Delete photo
    @DELETE("api/photos/{photoId}")
    suspend fun deletePhoto(@Path("photoId") photoId: String): Response<Unit>

    // Reorder photos
    @PUT("api/photos/reorder")
    suspend fun reorderPhotos(@Body request: ReorderPhotosRequest): Response<Unit>

    @POST("api/user/request-otp")
    suspend fun requestOtp(@Body request: RequestOtpRequest): Response<OtpResponse>

    @POST("api/user/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @PUT("api/user/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest,
    ): Response<ChangePasswordResponse>

    @GET("all")
    suspend fun getAllCountries(
        @Query("fields") fields: String = "name,languages",
    ): List<CountryResponse>

    @GET("languages")
    suspend fun getLanguages(): List<LanguageResponse>

    @DELETE("api/user/account")
    suspend fun deleteAccount(): Response<Unit>

    @POST("api/auth/facebook-login")
    suspend fun facebookLogin(@Body request: FacebookLoginRequest): FacebookLoginResponse

    // ============================================================
    // Discovery APIs - For Swiping UI (Simple, Fast)
    // ============================================================

    // Get next match card (single)
    @GET("api/discovery/next")
    suspend fun getNextMatchCard(
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
    ): Response<MatchCardResponse>

    // Get batch of match cards
    @GET("api/discovery/cards")
    suspend fun getMatchCards(
        @Query("limit") limit: Int = 10,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
    ): Response<MatchCardsListResponse>

    // Like/Swipe right (simple, no quota)
    @POST("api/discovery/like")
    suspend fun discoveryLike(@Body request: MatchActionRequest): Response<MatchResultResponse>

    // Pass/Swipe left
    @POST("api/discovery/pass")
    suspend fun discoveryPass(@Body request: MatchActionRequest): Response<Unit>

    // Send a super like (simple, no quota)
    @POST("api/discovery/superlike")
    suspend fun discoverySuperlike(@Body request: MatchActionRequest): Response<MatchResultResponse>

    // Block a user
    @POST("api/discovery/block")
    suspend fun discoveryBlock(@Body request: BlockUserRequest): Response<Unit>

    // Get all matches (paginated)
    @GET("api/discovery")
    suspend fun getDiscoveryMatches(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): Response<MatchesListResponse>

    // Get match by ID
    @GET("api/discovery/{matchId}")
    suspend fun getDiscoveryMatchById(
        @Path("matchId") matchId: String,
    ): Response<MatchResponse>

    // Unmatch with a user
    @POST("api/discovery/unmatch")
    suspend fun discoveryUnmatch(@Body request: UnmatchRequest): Response<Unit>

    // ============================================================
    // Match APIs - For Match Management (With Quota & Premium)
    // ============================================================

    // Get all your matches (simple list)
    @GET("api/matches")
    suspend fun getMatches(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): Response<MatchesListResponse>

    // Get specific match details
    @GET("api/matches/{matchId}")
    suspend fun getMatchById(
        @Path("matchId") matchId: String,
    ): Response<MatchResponse>

    // Perform action on match
    @POST("api/matches/action")
    suspend fun matchAction(@Body request: MatchActionRequest): Response<Unit>

    // Recommendation endpoints
    @GET("api/recommendations/criteria")
    suspend fun getRecommendationCriteria(): Response<RecommendationCriteriaResponse>

    @PUT("api/recommendations/criteria")
    suspend fun updateRecommendationCriteria(
        @Body request: RecommendationCriteriaRequest,
    ): Response<RecommendationCriteriaResponse>

    @Multipart
    @POST("api/verify-face")
    suspend fun verifyFace(
        @Part selfie: MultipartBody.Part,
    ): Response<VerifyFaceResponse>

    // FCM Token Management
    @PUT("api/user/fcm-token")
    suspend fun updateFCMToken(@Body request: UpdateFCMTokenRequest): Response<Unit>

    // Match liked you
    @GET("api/matches/status/{targetUserId}")
    suspend fun getMatchStatus(
        @Path("targetUserId") targetUserId: String,
    ): MatchStatusResponse

    @GET("api/matches/liked-you")
    suspend fun getUsersWhoLikedYou(): List<LikedYouResponseDto>

    @GET("api/conversations/matched-users")
    suspend fun getMatchedUsers(
        @Header("Authorization") token: String,
    ): List<MatchResponseDTO>

    @POST("api/chat/send")
    suspend fun sendMessage(@Body message: MessageModel): MessageModel

    @GET("api/chat/history/{matchId}")
    suspend fun getHistory(@Path("matchId") matchId: String): List<MessageModel>

    @GET("api/chat/rooms/{matchId}/last-message")
    suspend fun getLastMessage(@Path("matchId") matchId: String): LastMessageResponse

    @Multipart
    @POST("api/upload/audio")
    suspend fun uploadAudio(
        @Part audio: MultipartBody.Part,
    ): Response<UploadAudioResponse>

    @Multipart
    @POST("api/media/image")
    suspend fun uploadChatImage(
        @Part file: MultipartBody.Part,
    ): Response<UploadImageResponse>

    @DELETE("api/conversations/{matchId}")
    suspend fun deleteAllMessages(@Path("matchId") matchId: String)

    @POST("api/matches/unmatch")
    suspend fun unmatch(@Body request: UnmatchUserRequest): Response<Unit>

    @POST("api/block")
    suspend fun block(@Body request: BlockUserRequest): Response<Unit>

    @POST("api/block/unblock")
    suspend fun unblock(@Body request: BlockUserRequest): Response<Unit>
}
