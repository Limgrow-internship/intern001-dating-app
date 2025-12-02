package com.intern001.dating.domain.repository

import com.intern001.dating.domain.model.LikedYouUser

interface LikedYouRepository {
    suspend fun getUsersWhoLikedMe(): Result<List<LikedYouUser>>
}
