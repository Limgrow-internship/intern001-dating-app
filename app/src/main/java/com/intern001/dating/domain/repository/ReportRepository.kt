package com.intern001.dating.domain.repository

import com.intern001.dating.data.model.response.ReportResponse

interface ReportRepository {
    suspend fun createReport(
        userIdIsReported: String,
        reason: String,
    ): Result<ReportResponse>
}
