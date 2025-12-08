package com.intern001.dating.domain.usecase

import com.intern001.dating.domain.cache.InitialDataCache
import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.domain.model.UpdateProfile
import com.intern001.dating.domain.usecase.auth.GetCurrentUserUseCase
import com.intern001.dating.domain.usecase.match.GetMatchCardsUseCase
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PrefetchHomeDataUseCase @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getMatchCardsUseCase: GetMatchCardsUseCase,
    private val initialDataCache: InitialDataCache,
) {

    suspend operator fun invoke(matchLimit: Int = DEFAULT_MATCH_LIMIT) = coroutineScope {
        val profileDeferred = async<UpdateProfile?> {
            getCurrentUserUseCase().getOrNull()
        }

        val matchCardsDeferred = async<List<MatchCard>?> {
            getMatchCardsUseCase(matchLimit).getOrNull()
        }

        profileDeferred.await()?.let(initialDataCache::storeUserProfile)
        matchCardsDeferred.await()
            ?.takeIf { it.isNotEmpty() }
            ?.let(initialDataCache::storeMatchCards)
    }

    companion object {
        private const val DEFAULT_MATCH_LIMIT = 20
    }
}
