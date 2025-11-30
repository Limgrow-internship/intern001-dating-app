package com.intern001.dating.domain.repository

import com.intern001.dating.domain.model.MatchStatusGet

interface MatchStatusRepository {
    suspend fun getMatchStatus(targetUserId: String): MatchStatusGet
}
