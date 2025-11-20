package com.intern001.dating.presentation.ui.profile.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentViewProfileBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment

class ViewProfileFragment : BaseFragment() {

    private var _binding: FragmentViewProfileBinding? = null
    private val binding get() = _binding!!

    private val imageList = listOf(
        R.drawable.co4la,
        R.drawable.co4la,
        R.drawable.co4la,
    )

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

        setupViewPager()
        setupIndicatorBars(imageList.size)
        setCurrentIndicator(0)
        setupGoalsRecyclerView()
        setupInterestsRecyclerView()
    }

    private fun setupViewPager() {
        val adapter = ProfileImageAdapter(imageList)
        binding.viewPagerTop.adapter = adapter

        binding.viewPagerTop.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setCurrentIndicator(position)
            }
        })
    }

    private fun setupIndicatorBars(count: Int) {
        val container = binding.indicatorBars
        container.removeAllViews()
        container.weightSum = count.toFloat()

        for (i in 0 until count) {
            val bar = View(requireContext())
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
            )
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
            if (i == index) {
                bar.setBackgroundResource(R.drawable.bar_selected)
            } else {
                bar.setBackgroundResource(R.drawable.bar_unselected)
            }
        }
    }

    private fun setupGoalsRecyclerView() {
        val goalList = listOf(
            "Serious relationship",
            "New friends",
            "Long-term",
            "Something casual",
            "Hang out",
            "Travel",
            "Café meetup",
        )

        val layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        binding.rvGoals.layoutManager = layoutManager
        binding.rvGoals.adapter = ViewProfileGoalsAdapter(goalList)
    }

    private fun setupInterestsRecyclerView() {
        val goalList = listOf(
            "Music",
            "Photography",
            "Travel",
            "Deep talks",
        )

        val layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        binding.rvInterests.layoutManager = layoutManager
        binding.rvInterests.adapter = ViewProfileInterestsAdapter(goalList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
