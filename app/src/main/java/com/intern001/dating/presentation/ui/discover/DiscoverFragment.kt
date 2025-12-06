package com.intern001.dating.presentation.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    private val viewModel: DiscoverViewModel by activityViewModels()
    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private var currentCardView: MatchCardView? = null
    private var nextCardView: MatchCardView? = null
    private var alreadyLikedDialog: AlertDialog? = null

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

        (activity as? MainActivity)?.hideBottomNavigation(false)

        setupListeners()
        observeViewModel()

        viewModel.clearTemporaryMatchedAllowances()

        val currentCards = viewModel.matchCards.value
        val firstCardUserId = currentCards.firstOrNull()?.userId ?: ""
        val isMatchedUser = viewModel.isUserMatched(firstCardUserId)
        val hasOnlyMatchedUser = currentCards.size == 1 && isMatchedUser

        if (hasOnlyMatchedUser || currentCards.isEmpty()) {
            viewModel.loadMatchCards(showLoading = false)
        } else if (currentCards.isNotEmpty() && viewModel.hasMoreCards()) {
            showCurrentCard()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDistancesWithLatestLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        alreadyLikedDialog?.dismiss()
        alreadyLikedDialog = null
        _binding = null
    }

    private fun setupListeners() {
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        val b = _binding ?: return@collect
                        when (state) {
                            is UiState.Loading -> {
                                // Chỉ hiển thị loading nếu chưa có dữ liệu nào để show.
                                val shouldShowLoading = viewModel.matchCards.value.isEmpty()
                                b.progressBar.isVisible = shouldShowLoading
                                if (shouldShowLoading) {
                                    b.noMoreCardsLayout.isVisible = false
                                }
                            }
                            is UiState.Success -> {
                                b.progressBar.isVisible = false
                                if (state.data.isNotEmpty()) {
                                    showCurrentCard()
                                } else {
                                    b.noMoreCardsLayout.isVisible = true
                                }
                            }
                            is UiState.Error -> {
                                b.progressBar.isVisible = false
                                b.noMoreCardsLayout.isVisible = false
                                Toast.makeText(
                                    requireContext(),
                                    state.message ?: "An error occurred while loading cards",
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                            is UiState.Idle -> {
                                b.progressBar.isVisible = false
                            }
                        }
                    }
                }

                launch {
                    viewModel.matchCards.collect { cards ->
                        if (cards.isNotEmpty()) {
                            viewModel.getCurrentCard()?.let { currentCard ->
                                currentCardView?.updateDistance(currentCard.distance)
                            }
                            val nextIndex = viewModel.currentCardIndex.value + 1
                            if (nextIndex < cards.size) {
                                nextCardView?.updateDistance(cards[nextIndex].distance)
                            }
                            prepareNextCard()
                        }
                    }
                }

                launch {
                    viewModel.matchResult.collect { result ->
                        if (result?.isMatch == true) {
                            result.matchedUser?.let { matchedUser ->
                                result.matchId?.let { matchId ->
                                    showMatchOverlay(matchId, matchedUser.userId)
                                }
                            }
                        }
                    }
                }

                launch {
                    viewModel.currentCardIndex.collect {
                        showNextCard()
                    }
                }

                launch {
                    viewModel.alreadyLikedEvent.collect { card ->
                        showAlreadyLikedDialog(card.displayName)
                    }
                }
            }
        }
    }

    private fun showCurrentCard() {
        val currentCard = viewModel.getCurrentCard() ?: return

        binding.noMoreCardsLayout.isVisible = false

        currentCardView?.let { binding.cardContainer.removeView(it) }

        currentCardView = nextCardView ?: createCardView()
        nextCardView = null

        currentCardView?.let { cardView ->
            cardView.bindCard(currentCard)
            cardView.visibility = View.VISIBLE

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
            }
        })

        cardView.setOnOverlayTapListener(object : MatchCardView.OnOverlayTapListener {
            override fun onOverlayTap() {
                val currentCard = viewModel.getCurrentCard()
                val mode = currentCard?.relationshipMode ?: "dating"
                navigateToModeScreen(mode)
            }
        })

        cardView.setOnActionClickListener(object : MatchCardView.OnActionClickListener {
            override fun onBackClick() {
                viewModel.undoLastAction()
                showPreviousCard()
            }
        })
    }

    private fun navigateToModeScreen(mode: String) {
        val fragment = if (mode.lowercase() == "friend") {
            DatingModeFragment()
        } else {
            DatingModeFragment()
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.homeContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showMatchOverlay(matchId: String, matchedUserId: String) {
        val existingDialog = parentFragmentManager.findFragmentByTag("MatchOverlayDialog")
        if (existingDialog != null && existingDialog.isAdded) {
            return
        }

        val matchResult = viewModel.matchResult.replayCache.lastOrNull()
        val matchedUser = matchResult?.matchedUser

        val dialog = MatchOverlayDialog.newInstance(
            matchedUserId = matchedUserId,
            matchedUserPhotoUrl = matchedUser?.photos?.firstOrNull()?.url,
        )

        dialog.dialog?.setOnDismissListener {
            if (viewModel.hasMoreCards()) {
                showCurrentCard()
            } else {
                viewModel.loadMatchCards(showLoading = false)
            }
        }

        if (isAdded && parentFragmentManager != null) {
            dialog.show(parentFragmentManager, "MatchOverlayDialog")
        }
    }

    private fun showAlreadyLikedDialog(displayName: String?) {
        alreadyLikedDialog?.dismiss()
        alreadyLikedDialog =
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.already_liked_dialog_title)
                .setMessage(
                    getString(
                        R.string.already_liked_dialog_message,
                        displayName ?: getString(R.string.profile),
                    ),
                )
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }
}
