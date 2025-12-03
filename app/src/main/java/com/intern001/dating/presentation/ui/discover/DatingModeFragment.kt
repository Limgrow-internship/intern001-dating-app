package com.intern001.dating.presentation.ui.discover

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentDatingModeBinding
import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.presentation.common.state.UiState
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.discover.filter.FilterBottomSheet
import com.intern001.dating.presentation.ui.discover.view.SwipeableCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DatingModeFragment : BaseFragment() {

    private val viewModel: DiscoverViewModel by activityViewModels()
    private var _binding: FragmentDatingModeBinding? = null
    private val binding get() = _binding!!

    private var currentCardView: SwipeableCardView? = null
    private var nextCardView: SwipeableCardView? = null
    private var alreadyLikedDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDatingModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomNavigation(true)
        (parentFragment as? com.intern001.dating.presentation.ui.home.HomeFragment)?.hideTabBar(true)

        setupListeners()
        observeViewModel()

        val targetUserId = arguments?.getString("targetUserId")
        val targetListUserId = arguments?.getString("targetListUserId")
        val likerId = arguments?.getString("likerId")
        val allowMatchedProfile = arguments?.getBoolean("allowMatchedProfile") ?: false

        if (!targetListUserId.isNullOrBlank()) {
            viewLifecycleOwner.lifecycleScope.launch {
                val result = viewModel.fetchProfileForNavigation(
                    userId = targetListUserId,
                    allowMatched = allowMatchedProfile,
                )

                result.fold(
                    onSuccess = {
                        viewModel.setCurrentCardIndex(0)

                        delay(150)
                        showCurrentCard()
                    },
                    onFailure = {
                        Toast.makeText(requireContext(), "Không tải được profile", Toast.LENGTH_SHORT).show()
                    },
                )
            }
            return
        }

        if (!targetUserId.isNullOrBlank()) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.fetchProfileForNavigation(targetUserId, allowMatched = allowMatchedProfile)
                    .fold(
                        onSuccess = {
                            val index = viewModel.matchCards.value.indexOfFirst { it.userId == targetUserId }
                            Log.d("DEBUG_ARGS", "Fetched card.userId = ${it.userId}")
                            Log.d("DEBUG_ARGS", "Fetched card.id = ${it.id}")

                            if (index >= 0) {
                                viewModel.setCurrentCardIndex(index)
                                delay(150)
                                showCurrentCard()
                            } else if (viewModel.hasMoreCards()) {
                                showCurrentCard()
                            }
                        },
                        onFailure = {
                            val index = viewModel.matchCards.value.indexOfFirst { it.userId == targetUserId }
                            if (index >= 0) {
                                viewModel.setCurrentCardIndex(index)
                                delay(150)
                                showCurrentCard()
                            } else if (viewModel.hasMoreCards()) {
                                showCurrentCard()
                            }
                        },
                    )
            }
            return
        }
        if (!likerId.isNullOrBlank()) {
            viewLifecycleOwner.lifecycleScope.launch {
                val cards = viewModel.matchCards.filter { it.isNotEmpty() }.first()

                val likerIndex = cards.indexOfFirst { it.userId == likerId }

                viewModel.fetchAndAddProfileCard(likerId).fold(
                    onSuccess = {
                        delay(200)
                        showCurrentCard()
                    },
                    onFailure = {
                        if (likerIndex >= 0) {
                            viewModel.setCurrentCardIndex(likerIndex)
                            delay(200)
                            showCurrentCard()
                        } else if (viewModel.hasMoreCards()) {
                            showCurrentCard()
                        }
                    },
                )
            }
            return
        }
        if (viewModel.matchCards.value.isNotEmpty() && viewModel.hasMoreCards()) {
            showCurrentCard()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show tab bar again when leaving Dating Mode
        (parentFragment as? com.intern001.dating.presentation.ui.home.HomeFragment)?.hideTabBar(false)
        (activity as? MainActivity)?.hideBottomNavigation(false)
        alreadyLikedDialog?.dismiss()
        alreadyLikedDialog = null
        viewModel.clearTemporaryMatchedAllowances()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDistancesWithLatestLocation()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnFilter.setOnClickListener {
            FilterBottomSheet().show(parentFragmentManager, "FilterBottomSheet")
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
                        Toast.makeText(
                            requireContext(),
                            state.message ?: "An error occurred while loading cards",
                            Toast.LENGTH_LONG,
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

        viewLifecycleOwner.lifecycleScope.launch {
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

        val hasLikerId = !arguments?.getString("likerId").isNullOrBlank()
        if (!hasLikerId) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.currentCardIndex.collect {
                    if (viewModel.matchCards.value.isNotEmpty()) {
                        showCurrentCard()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.alreadyLikedEvent.collect { card ->
                showAlreadyLikedDialog(card.displayName)
            }
        }
    }

    private fun showCurrentCard() {
        val currentCard = viewModel.getCurrentCard() ?: return

        binding.noMoreCardsLayout.isVisible = false
        binding.detailInfoContainer.visibility = View.VISIBLE

        val container = binding.cardContainer

        currentCardView?.let { container.removeView(it) }

        currentCardView = nextCardView ?: createCardView()
        nextCardView = null
        currentCardView?.let { cardView ->
            cardView.bindCard(currentCard)
            cardView.visibility = View.VISIBLE

            if (cardView.parent == null) {
                container.addView(cardView, 0)
            }

            setupCardSwipeListener(cardView)
        }

        bindDetailInfo(currentCard)

        prepareNextCard()
    }

    private fun bindDetailInfo(card: MatchCard) {
        binding.detailScrollView.scrollTo(0, 0)

        if (!card.bio.isNullOrEmpty()) {
            binding.tvBio.text = card.bio
            binding.tvBio.visibility = View.VISIBLE
        } else {
            binding.tvBio.visibility = View.GONE
        }

        if (!card.occupation.isNullOrEmpty()) {
            binding.tvOccupation.text = card.occupation
            binding.occupationSection.visibility = View.VISIBLE
        } else {
            binding.occupationSection.visibility = View.GONE
        }

        if (!card.education.isNullOrEmpty()) {
            binding.tvEducation.text = card.education
            binding.educationSection.visibility = View.VISIBLE
        } else {
            binding.educationSection.visibility = View.GONE
        }

        card.location?.let { loc ->
            if (!loc.city.isNullOrEmpty()) {
                val locationText = buildString {
                    append(loc.city)
                    if (!loc.country.isNullOrEmpty()) {
                        append(", ")
                        append(loc.country)
                    }
                }
                binding.tvLocationDetail.text = locationText
                binding.locationSection.visibility = View.VISIBLE
            } else {
                binding.locationSection.visibility = View.GONE
            }
        } ?: run {
            binding.locationSection.visibility = View.GONE
        }

        if (!card.zodiacSign.isNullOrEmpty()) {
            binding.tvZodiac.text = card.zodiacSign
            binding.zodiacSection.visibility = View.VISIBLE
        } else {
            binding.zodiacSection.visibility = View.GONE
        }

        if (!card.relationshipMode.isNullOrEmpty()) {
            binding.tvLookingForTitle.visibility = View.VISIBLE
            binding.chipGroupLookingFor.visibility = View.VISIBLE
            binding.chipGroupLookingFor.removeAllViews()

            val chip = createChip(card.relationshipMode)
            binding.chipGroupLookingFor.addView(chip)
        } else {
            binding.tvLookingForTitle.visibility = View.GONE
            binding.chipGroupLookingFor.visibility = View.GONE
        }

        if (card.interests.isNotEmpty()) {
            binding.tvInterestsTitle.visibility = View.VISIBLE
            binding.chipGroupInterests.visibility = View.VISIBLE
            binding.chipGroupInterests.removeAllViews()

            card.interests.forEach { interest ->
                val chip = createChip(interest)
                binding.chipGroupInterests.addView(chip)
            }
        } else {
            binding.tvInterestsTitle.visibility = View.GONE
            binding.chipGroupInterests.visibility = View.GONE
        }
    }

    private fun createChip(text: String): Chip {
        return Chip(requireContext()).apply {
            this.text = text
            isClickable = false
            isCheckable = false
            setChipBackgroundColorResource(R.color.gray_100)
            setTextColor(context.getColor(R.color.black))
            chipStrokeWidth = 0f
        }
    }

    private fun prepareNextCard() {
        if (nextCardView != null) return

        val cards = viewModel.matchCards.value
        val nextIndex = viewModel.currentCardIndex.value + 1

        if (nextIndex < cards.size) {
            val container = binding.cardContainer
            nextCardView = createCardView().apply {
                bindCard(cards[nextIndex])
                visibility = View.VISIBLE
                container.addView(this, 0)
            }
        }
    }

    private fun showNextCard() {
        if (!viewModel.hasMoreCards()) {
            binding.noMoreCardsLayout.isVisible = true
            binding.detailInfoContainer.visibility = View.GONE
            return
        }
        showCurrentCard()
    }

    private fun showPreviousCard() {
        if (!viewModel.hasMoreCards()) {
            binding.noMoreCardsLayout.isVisible = false
        }
        binding.detailInfoContainer.visibility = View.VISIBLE
        showCurrentCard()
    }

    private fun createCardView(): SwipeableCardView {
        return SwipeableCardView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
        }
    }

    private fun setupCardSwipeListener(cardView: SwipeableCardView) {
        cardView.setOnSwipeListener(object : SwipeableCardView.OnSwipeListener {
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

        cardView.setOnPhotoClickListener(object : SwipeableCardView.OnPhotoClickListener {
            override fun onLongPress() {
                // Show full screen photo view
            }
        })

        cardView.setOnActionClickListener(object : SwipeableCardView.OnActionClickListener {
            override fun onBackClick() {
                viewModel.undoLastAction()
                showPreviousCard()
            }
        })
    }

    private fun showMatchOverlay(matchId: String, matchedUserId: String) {
        // Check if dialog is already showing
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
