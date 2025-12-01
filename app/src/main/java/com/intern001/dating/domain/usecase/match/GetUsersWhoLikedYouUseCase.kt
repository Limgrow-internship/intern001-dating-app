package com.intern001.dating.domain.usecase.match

import com.intern001.dating.domain.model.LikedYouUser
import com.intern001.dating.domain.repository.LikedYouRepository
import javax.inject.Inject

class GetUsersWhoLikedYouUseCase @Inject constructor(
    private val repository: LikedYouRepository
) {
    suspend operator fun invoke(): Result<List<LikedYouUser>> {
        return repository.getUsersWhoLikedMe()
    }
}
