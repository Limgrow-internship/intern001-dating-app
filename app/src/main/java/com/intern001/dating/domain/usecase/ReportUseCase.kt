package com.intern001.dating.domain.usecase

import com.intern001.dating.data.model.response.ReportResponse
import com.intern001.dating.domain.repository.ReportRepository
import javax.inject.Inject

class ReportUseCase @Inject constructor(
    private val repo: ReportRepository,
) {
    suspend operator fun invoke(
        userIdIsReported: String,
        reason: String,
    ): Result<ReportResponse> {
        return repo.createReport(userIdIsReported, reason)
    }
}
