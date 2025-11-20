package com.intern001.dating.presentation.ui.discover

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.domain.model.MatchResult
import com.intern001.dating.domain.usecase.match.GetMatchCardsUseCase
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
import kotlinx.coroutines.launch

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val getMatchCardsUseCase: GetMatchCardsUseCase,
    private val likeUserUseCase: LikeUserUseCase,
    private val passUserUseCase: PassUserUseCase,
    private val superLikeUserUseCase: SuperLikeUserUseCase,
) : BaseStateViewModel<List<MatchCard>>() {

    companion object {
        private const val TAG = "DiscoverViewModel"
    }

    private val _matchCards = MutableStateFlow<List<MatchCard>>(emptyList())
    val matchCards: StateFlow<List<MatchCard>> = _matchCards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _matchResult = MutableSharedFlow<MatchResult?>()
    val matchResult: SharedFlow<MatchResult?> = _matchResult.asSharedFlow()

    private val _undoStack = MutableStateFlow<List<MatchCard>>(emptyList())

    init {
        loadMatchCards()
    }

    fun loadMatchCards(limit: Int = 10) {
        viewModelScope.launch {
            Log.d(TAG, "🔄 Loading match cards with limit: $limit")
            setLoading()
            getMatchCardsUseCase(limit).fold(
                onSuccess = { cards ->
                    Log.d(TAG, "✅ Successfully loaded ${cards.size} match cards")
                    cards.forEachIndexed { index, card ->
                        Log.d(TAG, "  Card $index: ${card.userId} - ${card.firstName} ${card.lastName}, age: ${card.age}, photos: ${card.photos.size}")
                    }
                    _matchCards.value = cards
                    _currentCardIndex.value = 0
                    setSuccess(cards)
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Failed to load cards: ${error.message}", error)
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
}
