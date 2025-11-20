package com.intern001.dating.presentation.ui.discover

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentDiscoverBinding
import com.intern001.dating.presentation.common.state.UiState
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.discover.view.MatchCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DiscoverFragment : BaseFragment() {

    private val viewModel: DiscoverViewModel by viewModels()
    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private var currentCardView: MatchCardView? = null
    private var nextCardView: MatchCardView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show bottom navigation when in DiscoverFragment
        (activity as? MainActivity)?.hideBottomNavigation(false)

        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            viewModel.undoLastAction()
            showPreviousCard()
        }

        binding.btnDislike.setOnClickListener {
            currentCardView?.animateSwipeOut(MatchCardView.SwipeDirection.LEFT)
            viewModel.passUser()
        }

        binding.btnSuperLike.setOnClickListener {
            currentCardView?.animateSwipeOut(MatchCardView.SwipeDirection.UP)
            viewModel.superLikeUser()
        }

        binding.btnMatch.setOnClickListener {
            currentCardView?.animateSwipeOut(MatchCardView.SwipeDirection.RIGHT)
            viewModel.likeUser()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.noMoreCardsLayout.isVisible = false
                    }
                    is UiState.Success -> {
                        binding.progressBar.isVisible = false
                        if (state.data.isNotEmpty()) {
                            showCurrentCard()
                        } else {
                            binding.noMoreCardsLayout.isVisible = true
                        }
                    }
                    is UiState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.noMoreCardsLayout.isVisible = false
                        // Show error message
                        Toast.makeText(
                            requireContext(),
                            state.message ?: "An error occurred while loading cards",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is UiState.Idle -> {
                        binding.progressBar.isVisible = false
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.matchCards.collect { cards ->
                if (cards.isNotEmpty()) {
                    prepareNextCard()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.matchResult.collect { result ->
                if (result?.isMatch == true) {
                    // Navigate to Match Found Screen
                    result.matchedUser?.let { matchedUser ->
                        result.matchId?.let { matchId ->
                            navigateToMatchFound(matchId, matchedUser.userId)
                        }
                    }
                }
                showNextCard()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentCardIndex.collect { _ ->
                updateBackButtonState()
            }
        }
    }

    private fun showCurrentCard() {
        val currentCard = viewModel.getCurrentCard() ?: return

        // Remove old card view
        currentCardView?.let { binding.cardContainer.removeView(it) }

        // Use next card if available, otherwise create new
        currentCardView = nextCardView ?: createCardView()
        nextCardView = null

        currentCardView?.let { cardView ->
            cardView.bindCard(currentCard)
            cardView.visibility = View.VISIBLE

            // Add to container if not already added
            if (cardView.parent == null) {
                binding.cardContainer.addView(cardView, 0)
            }

            setupCardSwipeListener(cardView)
        }

        prepareNextCard()
    }

    private fun prepareNextCard() {
        if (nextCardView != null) return

        val cards = viewModel.matchCards.value
        val nextIndex = viewModel.currentCardIndex.value + 1

        if (nextIndex < cards.size) {
            nextCardView = createCardView().apply {
                bindCard(cards[nextIndex])
                visibility = View.VISIBLE
                binding.cardContainer.addView(this, 0)
            }
        }
    }

    private fun showNextCard() {
        if (!viewModel.hasMoreCards()) {
            binding.noMoreCardsLayout.isVisible = true
            return
        }
        showCurrentCard()
    }

    private fun showPreviousCard() {
        if (!viewModel.hasMoreCards()) {
            binding.noMoreCardsLayout.isVisible = false
        }
        showCurrentCard()
    }

    private fun createCardView(): MatchCardView {
        return MatchCardView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
        }
    }

    private fun setupCardSwipeListener(cardView: MatchCardView) {
        cardView.setOnSwipeListener(object : MatchCardView.OnSwipeListener {
            override fun onLike() {
                viewModel.likeUser()
            }

            override fun onDislike() {
                viewModel.passUser()
            }

            override fun onSuperLike() {
                viewModel.superLikeUser()
            }
        })

        cardView.setOnPhotoClickListener(object : MatchCardView.OnPhotoClickListener {
            override fun onLongPress() {
                // Show full screen photo view
                // TODO: Implement full screen photo viewer
            }
        })
    }

    private fun updateBackButtonState() {
        binding.btnBack.isEnabled = viewModel.currentCardIndex.value > 0
        binding.btnBack.alpha = if (binding.btnBack.isEnabled) 1.0f else 0.5f
    }

    private fun navigateToMatchFound(matchId: String, matchedUserId: String) {
        val matchResult = viewModel.matchResult.replayCache.lastOrNull()
        val matchedUser = matchResult?.matchedUser

        val fragment = MatchFoundFragment.newInstance(
            matchId = matchId,
            matchedUserId = matchedUserId,
            matchedUserName = matchedUser?.firstName ?: "",
            matchedUserPhotoUrl = matchedUser?.photos?.firstOrNull()?.url,
            currentUserPhotoUrl = null,
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.homeContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
