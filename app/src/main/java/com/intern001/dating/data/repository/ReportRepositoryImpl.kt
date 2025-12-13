package com.intern001.dating.data.repository

import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.model.request.ReportRequest
import com.intern001.dating.data.model.response.ReportResponse
import com.intern001.dating.domain.repository.ReportRepository
import javax.inject.Inject
import javax.inject.Named

class ReportRepositoryImpl @Inject constructor(
    @Named("datingApi") private val api: DatingApiService,
) : ReportRepository {

    override suspend fun createReport(
        userIdIsReported: String,
        reason: String,
    ): Result<ReportResponse> = try {
        val response = api.createReport(
            ReportRequest(
                userIdIsReported = userIdIsReported,
                reason = reason,
            ),
        )
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
