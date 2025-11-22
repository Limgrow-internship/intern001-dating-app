package com.intern001.dating.presentation.ui.profile.edit

import com.intern001.dating.presentation.ui.profile.edit.ProfileImageAdapter
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
import com.intern001.dating.domain.model.UserProfile
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
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

        setupGoalsRecyclerView(emptyList())
        setupInterestsRecyclerView(emptyList())

        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModel.getUserProfile()

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

    private fun bindProfileData(profile: UpdateProfile) {
        binding.tvNameAge.text = "${profile.displayName}, ${profile.age ?: ""}"
        binding.tvGender.text = profile.gender ?: ""
        binding.tvDescription.text = profile.bio ?: ""
        binding.tvMajor.text = profile.occupation ?: ""
        binding.tvEducation.text = profile.education ?: ""
        binding.tvLocation.text = profile.location?.city ?: ""
        binding.tvZodiac.text = profile.zodiacSign ?: ""

        profile.photos?.takeIf { it.isNotEmpty() }?.let {
            setupViewPager(it)
        }

        setupGoalsRecyclerView(profile.goals as? List<String> ?: emptyList())
        setupInterestsRecyclerView(profile.interests as? List<String> ?: emptyList())
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
                if (i == index) R.drawable.bar_selected else R.drawable.bar_unselected
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
