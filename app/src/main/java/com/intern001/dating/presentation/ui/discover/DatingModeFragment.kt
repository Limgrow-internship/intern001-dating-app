package com.intern001.dating.presentation.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentDatingModeBinding
import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.presentation.common.state.UiState
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.discover.view.SwipeableCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DatingModeFragment : BaseFragment() {

    private val viewModel: DiscoverViewModel by activityViewModels()
    private var _binding: FragmentDatingModeBinding? = null
    private val binding get() = _binding!!

    private var currentCardView: SwipeableCardView? = null
    private var nextCardView: SwipeableCardView? = null

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

        // Hide bottom navigation and tab bar in Dating Mode screen
        (activity as? MainActivity)?.hideBottomNavigation(true)
        (parentFragment as? com.intern001.dating.presentation.ui.home.HomeFragment)?.hideTabBar(true)

        setupListeners()
        observeViewModel()

        // Show current card if data already loaded
        if (viewModel.matchCards.value.isNotEmpty() && viewModel.hasMoreCards()) {
            showCurrentCard()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show tab bar again when leaving Dating Mode
        (parentFragment as? com.intern001.dating.presentation.ui.home.HomeFragment)?.hideTabBar(false)
        (activity as? MainActivity)?.hideBottomNavigation(false)
        _binding = null
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnFilter.setOnClickListener {
            // TODO: Open filter screen
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
                    prepareNextCard()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.matchResult.collect { result ->
                if (result?.isMatch == true) {
                    result.matchedUser?.let { matchedUser ->
                        result.matchId?.let { matchId ->
                            navigateToMatchFound(matchId, matchedUser.userId)
                        }
                    }
                }
            }
        }

        // Observe card index changes to update UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentCardIndex.collect {
                showNextCard()
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

            if (cardView.parent == null) {
                binding.cardContainer.addView(cardView, 0)
            }

            setupCardSwipeListener(cardView)
        }

        // Update detail info section
        bindDetailInfo(currentCard)

        prepareNextCard()
    }

    private fun bindDetailInfo(card: MatchCard) {
        // Reset scroll position
        binding.detailScrollView.scrollTo(0, 0)

        // Bio
        if (!card.bio.isNullOrEmpty()) {
            binding.tvBio.text = card.bio
            binding.tvBio.visibility = View.VISIBLE
        } else {
            binding.tvBio.visibility = View.GONE
        }

        // Occupation
        if (!card.occupation.isNullOrEmpty()) {
            binding.tvOccupation.text = card.occupation
            binding.occupationSection.visibility = View.VISIBLE
        } else {
            binding.occupationSection.visibility = View.GONE
        }

        // Education
        if (!card.education.isNullOrEmpty()) {
            binding.tvEducation.text = card.education
            binding.educationSection.visibility = View.VISIBLE
        } else {
            binding.educationSection.visibility = View.GONE
        }

        // Location detail
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

        // Zodiac
        if (!card.zodiacSign.isNullOrEmpty()) {
            binding.tvZodiac.text = card.zodiacSign
            binding.zodiacSection.visibility = View.VISIBLE
        } else {
            binding.zodiacSection.visibility = View.GONE
        }

        // Looking for chips
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

        // Interests chips
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
            binding.detailScrollView.visibility = View.GONE
            return
        }
        showCurrentCard()
    }

    private fun showPreviousCard() {
        if (!viewModel.hasMoreCards()) {
            binding.noMoreCardsLayout.isVisible = false
        }
        binding.detailScrollView.visibility = View.VISIBLE
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
