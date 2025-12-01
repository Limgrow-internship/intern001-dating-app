package com.intern001.dating.domain.usecase

import com.intern001.dating.domain.entity.LastMessageEntity
import com.intern001.dating.domain.repository.ChatRepository
import javax.inject.Inject

class GetLastMessageUseCase @Inject constructor(private val repo: ChatRepository) {
    suspend operator fun invoke(matchId: String): LastMessageEntity {
        return repo.getLastMessage(matchId)
    }
}
