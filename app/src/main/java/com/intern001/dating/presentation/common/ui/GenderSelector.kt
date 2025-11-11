package com.intern001.dating.presentation.common.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.intern001.dating.R
import com.intern001.dating.databinding.ViewGenderSelectorBinding

class GenderSelector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewGenderSelectorBinding =
        ViewGenderSelectorBinding.inflate(LayoutInflater.from(context), this, true)

    enum class Gender {
        MALE,
        FEMALE,
        OTHER,
        NONE,
    }

    private var selectedGender: Gender = Gender.NONE
    private var onGenderSelectedListener: ((Gender) -> Unit)? = null

    init {
        orientation = HORIZONTAL

        binding.btnMale.setOnClickListener {
            selectGender(Gender.MALE)
        }

        binding.btnFemale.setOnClickListener {
            selectGender(Gender.FEMALE)
        }

        binding.btnOther.setOnClickListener {
            selectGender(Gender.OTHER)
        }
    }

    private fun selectGender(gender: Gender) {
        selectedGender = gender
        updateUI()
        onGenderSelectedListener?.invoke(gender)
    }

    private fun updateUI() {
        val orangeColor = ContextCompat.getColor(context, R.color.bottom_nav_selected)
        val grayColor = ContextCompat.getColor(context, R.color.bottom_nav_unselected)
        val whiteColor = ContextCompat.getColor(context, R.color.white)
        val blackColor = ContextCompat.getColor(context, R.color.black)

        // Male button
        if (selectedGender == Gender.MALE) {
            binding.btnMale.setBackgroundResource(R.drawable.bg_button_orange)
            binding.btnMale.setTextColor(whiteColor)
        } else {
            binding.btnMale.setBackgroundResource(R.drawable.bg_gender_unselected)
            binding.btnMale.setTextColor(blackColor)
        }

        // Female button
        if (selectedGender == Gender.FEMALE) {
            binding.btnFemale.setBackgroundResource(R.drawable.bg_button_orange)
            binding.btnFemale.setTextColor(whiteColor)
        } else {
            binding.btnFemale.setBackgroundResource(R.drawable.bg_gender_unselected)
            binding.btnFemale.setTextColor(blackColor)
        }

        // Other button
        if (selectedGender == Gender.OTHER) {
            binding.btnOther.setBackgroundResource(R.drawable.bg_button_orange)
            binding.btnOther.setTextColor(whiteColor)
        } else {
            binding.btnOther.setBackgroundResource(R.drawable.bg_gender_unselected)
            binding.btnOther.setTextColor(blackColor)
        }
    }

    fun setOnGenderSelectedListener(listener: (Gender) -> Unit) {
        onGenderSelectedListener = listener
    }

    fun getSelectedGender(): Gender = selectedGender

    fun setSelectedGender(gender: Gender) {
        selectedGender = gender
        updateUI()
    }
}
