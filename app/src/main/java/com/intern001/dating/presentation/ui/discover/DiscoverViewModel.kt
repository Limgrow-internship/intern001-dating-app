package com.intern001.dating.presentation.ui.discover

import android.location.Location
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.domain.model.MatchResult
import com.intern001.dating.domain.model.UserLocation
import com.intern001.dating.domain.usecase.ObserveLocationUpdatesUseCase
import com.intern001.dating.domain.usecase.RefreshLocationUseCase
import com.intern001.dating.domain.usecase.match.GetMatchCardsUseCase
import com.intern001.dating.domain.usecase.match.GetProfileByUserIdUseCase
import com.intern001.dating.domain.usecase.match.LikeUserUseCase
import com.intern001.dating.domain.usecase.match.PassUserUseCase
import com.intern001.dating.domain.usecase.match.SuperLikeUserUseCase
import com.intern001.dating.presentation.common.viewmodel.BaseStateViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val getMatchCardsUseCase: GetMatchCardsUseCase,
    private val getProfileByUserIdUseCase: GetProfileByUserIdUseCase,
    private val likeUserUseCase: LikeUserUseCase,
    private val passUserUseCase: PassUserUseCase,
    private val superLikeUserUseCase: SuperLikeUserUseCase,
    private val refreshLocationUseCase: RefreshLocationUseCase,
    private val observeLocationUpdatesUseCase: ObserveLocationUpdatesUseCase,
) : BaseStateViewModel<List<MatchCard>>() {

    private val _matchCards = MutableStateFlow<List<MatchCard>>(emptyList())
    val matchCards: StateFlow<List<MatchCard>> = _matchCards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _matchResult = MutableSharedFlow<MatchResult?>()
    val matchResult: SharedFlow<MatchResult?> = _matchResult.asSharedFlow()

    private val _undoStack = MutableStateFlow<List<MatchCard>>(emptyList())

    private var isInitialized = false

    private var latestUserLocation: UserLocation? = null

    init {
        startRealtimeLocationUpdates()
        loadMatchCardsIfNeeded()
    }

    private fun loadMatchCardsIfNeeded() {
        if (!isInitialized) {
            isInitialized = true
            loadMatchCards()
        }
    }

    fun loadMatchCards(limit: Int = 10) {
        viewModelScope.launch {
            runCatching { refreshLocationUseCase() }
                .onFailure {
                    Log.w(TAG, "Unable to refresh location before fetching match cards", it)
                }
            setLoading()
            getMatchCardsUseCase(limit).fold(
                onSuccess = { cards ->
                    val adjustedCards = latestUserLocation?.let {
                        applyRealtimeDistances(cards, it)
                    } ?: cards
                    _matchCards.value = adjustedCards
                    _currentCardIndex.value = 0
                    setSuccess(adjustedCards)
                },
                onFailure = { error ->
                    setError(error.message ?: "Failed to load cards")
                },
            )
        }
    }

    fun getCurrentCard(): MatchCard? {
        val cards = _matchCards.value
        val index = _currentCardIndex.value
        return if (index < cards.size) cards[index] else null
    }

    fun likeUser() {
        val currentCard = getCurrentCard() ?: return
        viewModelScope.launch {
            likeUserUseCase(currentCard.userId).fold(
                onSuccess = { result ->
                    _matchResult.emit(result)
                    moveToNextCard(currentCard)
                },
                onFailure = {
                    // Handle error silently or show toast
                    moveToNextCard(currentCard)
                },
            )
        }
    }

    fun passUser() {
        val currentCard = getCurrentCard() ?: return
        viewModelScope.launch {
            passUserUseCase(currentCard.userId).fold(
                onSuccess = {
                    moveToNextCard(currentCard)
                },
                onFailure = {
                    // Handle error silently or show toast
                    moveToNextCard(currentCard)
                },
            )
        }
    }

    fun superLikeUser() {
        val currentCard = getCurrentCard() ?: return
        viewModelScope.launch {
            superLikeUserUseCase(currentCard.userId).fold(
                onSuccess = { result ->
                    _matchResult.emit(result)
                    moveToNextCard(currentCard)
                },
                onFailure = {
                    // Handle error silently or show toast
                    moveToNextCard(currentCard)
                },
            )
        }
    }

    private fun moveToNextCard(removedCard: MatchCard) {
        _undoStack.value = _undoStack.value + removedCard
        _currentCardIndex.value += 1

        // Load more cards if running low
        val remaining = _matchCards.value.size - _currentCardIndex.value
        if (remaining <= 2) {
            loadMatchCards()
        }
    }

    fun undoLastAction() {
        val undoStack = _undoStack.value
        if (undoStack.isNotEmpty() && _currentCardIndex.value > 0) {
            _currentCardIndex.value -= 1
            _undoStack.value = undoStack.dropLast(1)
        }
    }

    fun hasMoreCards(): Boolean {
        return _currentCardIndex.value < _matchCards.value.size
    }

    fun setCurrentCardIndex(index: Int) {
        if (index >= 0 && index < _matchCards.value.size) {
            _currentCardIndex.value = index
        }
    }

    suspend fun fetchAndAddProfileCard(userId: String): Result<MatchCard> {
        return try {
            getProfileByUserIdUseCase(userId).fold(
                onSuccess = { card ->
                    val currentCards = _matchCards.value.toMutableList()
                    val existingIndex = currentCards.indexOfFirst { it.userId == userId }

                    if (existingIndex >= 0) {
                        currentCards.removeAt(existingIndex)
                        currentCards.add(0, card)
                        _matchCards.value = currentCards
                        _currentCardIndex.value = 0
                    } else {
                        currentCards.add(0, card)
                        _matchCards.value = currentCards
                        _currentCardIndex.value = 0
                    }
                    Result.success(card)
                },
                onFailure = { error ->
                    Result.failure(error)
                },
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun startRealtimeLocationUpdates() {
        viewModelScope.launch {
            observeLocationUpdatesUseCase()
                .catch { throwable ->
                    Log.w(TAG, "Realtime location updates failed", throwable)
                }
                .collect { locationData ->
                    val userLocation = locationData.location ?: return@collect
                    latestUserLocation = userLocation
                    applyRealtimeDistances(userLocation)
                }
        }
    }

    private fun applyRealtimeDistances(userLocation: UserLocation) {
        val currentCards = _matchCards.value
        if (currentCards.isEmpty()) return
        val updatedCards = applyRealtimeDistances(currentCards, userLocation)
        if (updatedCards !== currentCards) {
            _matchCards.value = updatedCards
            setSuccess(updatedCards)
        }
    }

    private fun applyRealtimeDistances(
        cards: List<MatchCard>,
        userLocation: UserLocation,
    ): List<MatchCard> {
        if (cards.isEmpty()) return cards
        var changed = false
        val updatedCards = cards.map { card ->
            val cardLocation = card.location
            if (cardLocation != null) {
                val distanceKm = calculateDistanceKm(userLocation, cardLocation)
                if (distanceKm != null && hasMeaningfulDifference(card.distance, distanceKm)) {
                    changed = true
                    card.copy(distance = distanceKm)
                } else {
                    card
                }
            } else {
                card
            }
        }
        return if (changed) updatedCards else cards
    }

    private fun calculateDistanceKm(
        origin: UserLocation,
        destination: UserLocation,
    ): Double? {
        val results = FloatArray(1)
        Location.distanceBetween(
            origin.latitude,
            origin.longitude,
            destination.latitude,
            destination.longitude,
            results,
        )
        val meters = results.getOrNull(0) ?: return null
        if (meters.isNaN()) return null
        return meters.toDouble() / 1000
    }

    private fun hasMeaningfulDifference(oldDistance: Double?, newDistance: Double): Boolean {
        if (oldDistance == null) return true
        return kotlin.math.abs(oldDistance - newDistance) >= DISTANCE_DIFF_THRESHOLD_KM
    }

    fun refreshDistancesWithLatestLocation() {
        viewModelScope.launch {
            runCatching { refreshLocationUseCase() }
                .onSuccess { locationData ->
                    val userLocation = locationData.location
                    if (userLocation != null) {
                        latestUserLocation = userLocation
                        applyRealtimeDistances(userLocation)
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "Unable to refresh location for realtime distance", error)
                }
        }
    }

    companion object {
        private const val TAG = "DiscoverViewModel"
        private const val DISTANCE_DIFF_THRESHOLD_KM = 0.05
    }
}
