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
        updateButtonState(binding.btnMale, selectedGender == Gender.MALE)
        updateButtonState(binding.btnFemale, selectedGender == Gender.FEMALE)
        updateButtonState(binding.btnOther, selectedGender == Gender.OTHER)
    }

    private fun updateButtonState(button: com.google.android.material.button.MaterialButton, isSelected: Boolean) {
        val whiteColor = ContextCompat.getColor(context, R.color.white)
        val blackColor = ContextCompat.getColor(context, R.color.black)

        if (isSelected) {
            button.setBackgroundResource(R.drawable.bg_button_orange)
            button.setTextColor(whiteColor)
        } else {
            button.setBackgroundResource(R.drawable.bg_gender_unselected)
            button.setTextColor(blackColor)
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
