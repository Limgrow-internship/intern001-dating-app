package com.intern001.dating.data.repository

import android.util.Log
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.request.BlockUserRequest
import com.intern001.dating.data.model.request.MatchActionRequest
import com.intern001.dating.data.model.request.UnmatchRequest
import com.intern001.dating.data.model.response.toMatch
import com.intern001.dating.data.model.response.toMatchCard
import com.intern001.dating.data.model.response.toMatchList
import com.intern001.dating.data.model.response.toMatchResult
import com.intern001.dating.domain.model.Match
import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.domain.model.MatchList
import com.intern001.dating.domain.model.MatchResult
import com.intern001.dating.domain.repository.LocationRepository
import com.intern001.dating.domain.repository.MatchRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class MatchRepositoryImpl
@Inject
constructor(
    @Named("datingApi") private val apiService: DatingApiService,
    private val tokenManager: TokenManager,
    private val locationRepository: LocationRepository,
) : MatchRepository {
    companion object {
        private const val TAG = "MatchRepositoryImpl"
    }

    override suspend fun getNextMatchCard(): Result<MatchCard?> {
        return try {
            // Get location before API call (using LocationRepository instead of LocationService)
            val location = locationRepository.getUserLocation()

            val response = apiService.getNextMatchCard(
                latitude = location?.latitude,
                longitude = location?.longitude,
            )
            if (response.isSuccessful) {
                val matchCard = response.body()?.toMatchCard()
                Result.success(matchCard)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to get next match card"
                Log.e(TAG, "getNextMatchCard error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getNextMatchCard exception", e)
            Result.failure(e)
        }
    }

    override suspend fun getMatchCards(limit: Int): Result<List<MatchCard>> {
        return try {
            // Get location before API call (using LocationRepository instead of LocationService)
            val location = locationRepository.getUserLocation()

            val response = apiService.getMatchCards(
                limit = limit,
                latitude = location?.latitude,
                longitude = location?.longitude,
            )
            if (response.isSuccessful) {
                val cards = response.body()?.cards?.map { it.toMatchCard() } ?: emptyList()
                Result.success(cards)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to get match cards"
                Log.e(TAG, "getMatchCards error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMatchCards exception", e)
            Result.failure(e)
        }
    }

    override suspend fun likeUser(targetUserId: String): Result<MatchResult> {
        return try {
            val request = MatchActionRequest(targetUserId = targetUserId)
            val response = apiService.discoveryLike(request)
            if (response.isSuccessful) {
                val matchResult = response.body()?.toMatchResult()
                if (matchResult != null) {
                    Result.success(matchResult)
                } else {
                    Result.failure(Exception("Invalid response from server"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to like user"
                Log.e(TAG, "likeUser error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "likeUser exception", e)
            Result.failure(e)
        }
    }

    override suspend fun passUser(targetUserId: String): Result<Unit> {
        return try {
            val request = MatchActionRequest(targetUserId = targetUserId)
            val response = apiService.discoveryPass(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to pass user"
                Log.e(TAG, "passUser error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "passUser exception", e)
            Result.failure(e)
        }
    }

    override suspend fun superLikeUser(targetUserId: String): Result<MatchResult> {
        return try {
            val request = MatchActionRequest(targetUserId = targetUserId)
            val response = apiService.discoverySuperlike(request)
            if (response.isSuccessful) {
                val matchResult = response.body()?.toMatchResult()
                if (matchResult != null) {
                    Result.success(matchResult)
                } else {
                    Result.failure(Exception("Invalid response from server"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to super like user"
                Log.e(TAG, "superLikeUser error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "superLikeUser exception", e)
            Result.failure(e)
        }
    }

    override suspend fun blockUser(targetUserId: String): Result<Unit> {
        return try {
            val req = BlockUserRequest(targetUserId)
            apiService.block(req)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMatches(
        page: Int,
        limit: Int,
    ): Result<List<Match>> {
        return try {
            val response = apiService.getMatches(page, limit)
            if (response.isSuccessful) {
                val matches = response.body()?.matches?.map { it.toMatch() } ?: emptyList()
                Result.success(matches)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to get matches"
                Log.e(TAG, "getMatches error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMatches exception", e)
            Result.failure(e)
        }
    }

    override suspend fun getMatchById(matchId: String): Result<Match> {
        return try {
            val response = apiService.getMatchById(matchId)
            if (response.isSuccessful) {
                val match = response.body()?.toMatch()
                if (match != null) {
                    Result.success(match)
                } else {
                    Result.failure(Exception("Match not found"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to get match"
                Log.e(TAG, "getMatchById error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMatchById exception", e)
            Result.failure(e)
        }
    }

    override suspend fun getProfileByUserId(userId: String): Result<MatchCard> {
        return try {
            val response = apiService.getProfileByUserId(userId)
            if (response.isSuccessful) {
                val matchCard = response.body()?.toMatchCard()
                if (matchCard != null) {
                    Result.success(matchCard)
                } else {
                    Result.failure(Exception("Profile response body is null"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to get profile"
                Log.e(TAG, "getProfileByUserId error: ${response.code()} - $errorMessage")
                Result.failure(Exception("Failed to get profile: ${response.code()} - $errorMessage"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getProfileByUserId exception", e)
            Result.failure(e)
        }
    }

    override suspend fun unmatch(matchId: String): Result<Unit> {
        return try {
            val request = UnmatchRequest(matchId = matchId)
            val response = apiService.discoveryUnmatch(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to unmatch"
                Log.e(TAG, "unmatch error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "unmatch exception", e)
            Result.failure(e)
        }
    }
    override suspend fun getMatchedUsers(token: String): List<MatchList> = apiService.getMatchedUsers("Bearer $token").map { it.toMatchList() }
}
