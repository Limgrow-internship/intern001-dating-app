package com.intern001.dating.domain.usecase.match

import com.intern001.dating.domain.model.MatchList
import com.intern001.dating.domain.repository.MatchRepository
import javax.inject.Inject

class GetMatchedUsersUseCase @Inject constructor(private val matchRepository: MatchRepository) {
    suspend operator fun invoke(token: String): List<MatchList> {
        return matchRepository.getMatchedUsers(token)
    }
}
