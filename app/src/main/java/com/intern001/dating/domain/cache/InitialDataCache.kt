package com.intern001.dating.domain.cache

import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.domain.model.UpdateProfile
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple in-memory cache to warm up data before user lands on Home/Profile screens.
 * Data is stored temporarily and can be consumed by presentation layer for instant rendering.
 */
@Singleton
class InitialDataCache @Inject constructor() {

    private val matchCardsRef = AtomicReference<List<MatchCard>?>(null)
    private val userProfileRef = AtomicReference<UpdateProfile?>(null)

    fun storeMatchCards(cards: List<MatchCard>) {
        matchCardsRef.set(cards)
    }

    fun consumeMatchCards(): List<MatchCard>? = matchCardsRef.getAndSet(null)

    fun storeUserProfile(profile: UpdateProfile) {
        userProfileRef.set(profile)
    }

    fun consumeUserProfile(): UpdateProfile? = userProfileRef.getAndSet(null)

    fun clear() {
        matchCardsRef.set(null)
        userProfileRef.set(null)
    }
}
