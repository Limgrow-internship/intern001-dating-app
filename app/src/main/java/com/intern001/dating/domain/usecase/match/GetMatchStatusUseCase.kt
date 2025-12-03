package com.intern001.dating.domain.usecase.match

import com.intern001.dating.domain.model.MatchStatusGet
import com.intern001.dating.domain.repository.MatchStatusRepository
import javax.inject.Inject

class GetMatchStatusUseCase @Inject constructor(
    private val repository: MatchStatusRepository,
) {
    suspend operator fun invoke(targetUserId: String): MatchStatusGet {
        return repository.getMatchStatus(targetUserId)
    }
}
