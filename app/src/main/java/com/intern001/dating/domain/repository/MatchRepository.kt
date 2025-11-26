package com.intern001.dating.domain.repository

import com.intern001.dating.domain.model.Match
import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.domain.model.MatchResult

interface MatchRepository {
    suspend fun getNextMatchCard(): Result<MatchCard?>

    suspend fun getMatchCards(limit: Int = 10): Result<List<MatchCard>>

    suspend fun likeUser(targetUserId: String): Result<MatchResult>

    suspend fun passUser(targetUserId: String): Result<Unit>

    suspend fun superLikeUser(targetUserId: String): Result<MatchResult>

    suspend fun blockUser(targetUserId: String, reason: String? = null): Result<Unit>

    suspend fun getMatches(page: Int = 1, limit: Int = 20): Result<List<Match>>

    suspend fun getMatchById(matchId: String): Result<Match>

    suspend fun unmatch(matchId: String): Result<Unit>

    suspend fun getProfileByUserId(userId: String): Result<MatchCard>
}
