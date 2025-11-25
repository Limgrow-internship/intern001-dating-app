package com.intern001.dating.presentation.ui.discover.filter

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.slider.RangeSlider
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentFriendFilterBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment

class FriendFilterFragment: BaseFragment() {
    private var _binding: FragmentFriendFilterBinding? = null
    private val binding get() = _binding!!
    private val genderViews = mutableListOf<TextView>()
    private val selectedGoals = mutableSetOf<TextView>()
    private val selectedInterests = mutableSetOf<TextView>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSliders()
        setupGenderChips()
        setupGoalsChips()
        setupInterestsChips()
    }

    private fun setupSliders() {
        setupRangeSlider(binding.sliderAge, binding.tvAgeValue, "")
        setupRangeSlider(binding.sliderHeight, binding.tvHeightValue, "cm")
        setupRangeSlider(binding.sliderWeight, binding.tvWeightValue, "kg")
        setupRangeSlider(binding.sliderDistance, binding.tvDistanceValue, "km")
    }

    private fun setupRangeSlider(slider: RangeSlider, labelView: TextView, unit: String) {
        val thumbSize = 64

        val thumbDrawable = ShapeDrawable(OvalShape()).apply {
            intrinsicWidth = thumbSize
            intrinsicHeight = thumbSize
            paint.color = Color.parseColor("#FF6A00")
        }

        slider.setCustomThumbDrawable(thumbDrawable)

        slider.thumbRadius = thumbSize / 2
        slider.thumbElevation = 0f

        labelView.text = "${slider.values[0].toInt()} - ${slider.values[1].toInt()}$unit"

        slider.addOnChangeListener { _, _, _ ->
            val min = slider.values[0].toInt()
            val max = slider.values[1].toInt()
            labelView.text = "$min - $max$unit"
        }
    }


    private fun setupGenderChips() {
        genderViews.clear()
        genderViews.add(binding.llGenders.findViewById(R.id.tvMale))
        genderViews.add(binding.llGenders.findViewById(R.id.tvFemale))
        genderViews.add(binding.llGenders.findViewById(R.id.tvOther))

        genderViews.forEach { chip ->
            chip.setOnClickListener { selectGender(chip) }
        }
    }

    private fun selectGender(selectedChip: TextView) {
        genderViews.forEach { chip ->
            if (chip == selectedChip) {
                chip.setBackgroundResource(R.drawable.chip_selected)
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                chip.setBackgroundResource(R.drawable.chip_unselected)
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
    }
    private fun setupGoalsChips() {
        val g = binding.includeGoals
        val goalViews = listOf(
            g.tvGoalSerious,
            g.tvGoalFriends,
            g.tvGoalCasual,
            g.tvGoalVibing,
            g.tvGoalOpen,
            g.tvGoalFiguring
        )

        goalViews.forEach { chip ->
            chip.setOnClickListener { toggleMultiSelection(chip, selectedGoals) }
        }
    }

    private fun setupInterestsChips() {
        val i = binding.includeInterests
        val interestViews = listOf(
            i.tvInterestMusic,
            i.tvInterestPhotography,
            i.tvInterestTravel,
            i.tvInterestDeepTalks,
            i.tvInterestReadBook,
            i.tvInterestWalking,
            i.tvInterestPets,
            i.tvInterestCooking
        )

        interestViews.forEach { chip ->
            chip.setOnClickListener { toggleMultiSelection(chip, selectedInterests) }
        }
    }

    private fun toggleMultiSelection(chip: TextView, selectedSet: MutableSet<TextView>) {
        if (selectedSet.contains(chip)) {
            selectedSet.remove(chip)
            chip.setBackgroundResource(R.drawable.chip_unselected)
            chip.setTextColor(Color.BLACK)
        } else {
            selectedSet.add(chip)
            chip.setBackgroundResource(R.drawable.chip_selected)
            chip.setTextColor(Color.WHITE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
