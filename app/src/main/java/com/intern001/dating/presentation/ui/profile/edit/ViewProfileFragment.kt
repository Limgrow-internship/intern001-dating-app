package com.intern001.dating.presentation.ui.profile.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentViewProfileBinding
import com.intern001.dating.domain.model.UpdateProfile
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.profile.edit.ProfileImageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewProfileFragment : BaseFragment() {

    private var _binding: FragmentViewProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentViewProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.llMore.setOnClickListener {
            if (binding.layoutMoreInfo.visibility == View.GONE) {
                binding.layoutMoreInfo.visibility = View.VISIBLE
                binding.tvMore.text = "Less"
            } else {
                binding.layoutMoreInfo.visibility = View.GONE
                binding.tvMore.text = "More"
            }
        }

        setupGoalsRecyclerView(emptyList())
        setupInterestsRecyclerView(emptyList())

        observeUserProfileState()
        observeProfileUpdates()
        loadUserProfile()
    }

    override fun onResume() {
        super.onResume()
        // Refresh profile when fragment becomes visible
        if (isVisible) {
            refreshProfile()
        }
    }

    private fun loadUserProfile() {
        viewModel.getUserProfile()
    }

    fun refreshProfile() {
        // Force refresh by calling getUserProfile again
        // The cache should be updated after profile update, so this will get fresh data
        viewModel.getUserProfile()
    }

    private fun observeUserProfileState() {
        lifecycleScope.launch {
            viewModel.userProfileState.collectLatest { state ->
                when (state) {
                    is EditProfileViewModel.UiState.Loading -> {
                        // TODO: show loading indicator
                    }
                    is EditProfileViewModel.UiState.Success<*> -> {
                        bindProfileData(state.data as UpdateProfile)
                    }
                    is EditProfileViewModel.UiState.Error -> {
                        // TODO: show error message
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun observeProfileUpdates() {
        // Refresh profile when update succeeds
        lifecycleScope.launch {
            viewModel.updateProfileState.collectLatest { state ->
                when (state) {
                    is EditProfileViewModel.UiState.Success<*> -> {
                        // Profile was updated, bind the updated data immediately
                        val updatedProfile = state.data as? UpdateProfile
                        if (updatedProfile != null) {
                            bindProfileData(updatedProfile)
                        }
                        // Also refresh from server to ensure we have the latest data
                        if (isVisible) {
                            refreshProfile()
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun bindProfileData(profile: UpdateProfile) {
        val nameAgeText = when {
            profile.displayName != null && profile.age != null -> "${profile.displayName}, ${profile.age}"
            profile.displayName != null -> profile.displayName
            profile.age != null -> "${profile.age}"
            else -> ""
        }
        binding.tvNameAge.text = nameAgeText
        binding.tvGender.text = profile.gender ?: ""
        binding.tvDescription.text = profile.bio ?: "Chưa cập nhật bio"
        binding.tvMajor.text = profile.occupation ?: "Chưa cập nhật nghê nghiệp"
        binding.tvEducation.text = profile.education ?: "Chưa cập nhật trường học"
        binding.tvLocation.text = profile.city ?: "Chưa cập nhật thành phố"
        binding.tvZodiac.text = profile.zodiacSign ?: "Chưa cập nhật cung hoàng đạo"
        binding.tvHeight.text =
            profile.height?.let { "Chiều cao: $it" } ?: "Chiều cao: "
        binding.tvWeight.text =
            profile.weight?.let { "Cân nặng: $it" } ?: "Cân nặng: "

        // profile.photos is now List<Photo> (objects), extract URLs
        val photoUrls = profile.photos.map { it.url }
        if (photoUrls.isNotEmpty()) {
            setupViewPager(photoUrls)
        }

        // goals is already List<String>, no need to cast
        setupGoalsRecyclerView(profile.goals)
        setupInterestsRecyclerView(profile.interests)

        val questionMap = profile.openQuestionAnswers
        if (!questionMap.isNullOrEmpty()) {
            val firstKey = questionMap.keys.first()
            val firstAnswer = questionMap[firstKey] ?: ""

            val whatQuestions = resources.getStringArray(R.array.open_question_list_what)
                .map {
                    val parts = it.split("|", limit = 2)
                    parts[0] to parts[1]
                }

            val idealQuestions = resources.getStringArray(R.array.open_question_list_ideal)
                .map {
                    val parts = it.split("|", limit = 2)
                    parts[0] to parts[1]
                }

            val questionText =
                whatQuestions.firstOrNull { it.first == firstKey }?.second
                    ?: idealQuestions.firstOrNull { it.first == firstKey }?.second
                    ?: firstKey

            binding.tvQuestion.text = questionText
            binding.tvAnswer.text = firstAnswer

            binding.tvQuestion.visibility = View.VISIBLE
            binding.tvAnswer.visibility = View.VISIBLE
        } else {
            binding.tvQuestion.visibility = View.GONE
            binding.tvAnswer.visibility = View.GONE
        }
    }

    private fun setupViewPager(photoUrls: List<String>) {
        val adapter = ProfileImageAdapter(photoUrls)
        binding.viewPagerTop.adapter = adapter

        binding.viewPagerTop.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setCurrentIndicator(position)
            }
        })
        setupIndicatorBars(photoUrls.size)
        setCurrentIndicator(0)
    }

    private fun setupIndicatorBars(count: Int) {
        val container = binding.indicatorBars
        container.removeAllViews()
        container.weightSum = count.toFloat()

        for (i in 0 until count) {
            val bar = View(requireContext())
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
            params.weight = 1f
            params.marginEnd = 12
            bar.layoutParams = params
            bar.setBackgroundResource(R.drawable.bar_unselected)
            container.addView(bar)
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val container = binding.indicatorBars
        for (i in 0 until container.childCount) {
            val bar = container.getChildAt(i)
            bar.setBackgroundResource(
                if (i == index) R.drawable.bar_selected else R.drawable.bar_unselected,
            )
        }
    }

    private fun setupGoalsRecyclerView(goals: List<String>) {
        val layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }
        binding.rvGoals.layoutManager = layoutManager
        binding.rvGoals.adapter = ViewProfileGoalsAdapter(goals)
    }

    private fun setupInterestsRecyclerView(interests: List<String>) {
        val layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }
        binding.rvInterests.layoutManager = layoutManager
        binding.rvInterests.adapter = ViewProfileInterestsAdapter(interests)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
