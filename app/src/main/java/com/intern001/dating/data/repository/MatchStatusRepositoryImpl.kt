package com.intern001.dating.data.repository

import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.model.response.toDomain
import com.intern001.dating.domain.model.MatchStatusGet
import com.intern001.dating.domain.repository.MatchStatusRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

class MatchStatusRepositoryImpl @Inject constructor(
    @Named("datingApi") private val apiService: DatingApiService,
) : MatchStatusRepository {

    override suspend fun getMatchStatus(targetUserId: String): MatchStatusGet {
        return apiService.getMatchStatus(targetUserId).toDomain()
    }
}
