package com.intern001.dating.domain.usecase.match

import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.domain.repository.MatchRepository
import javax.inject.Inject

class GetNextMatchCardUseCase
@Inject
constructor(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(): Result<MatchCard?> {
        return try {
            matchRepository.getNextMatchCard()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
