package com.intern001.dating.presentation.ui.discover.filter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intern001.dating.databinding.FilterBottomSheetBinding
import com.intern001.dating.R

class FilterBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FilterBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FilterBottomSheetBinding.inflate(inflater, container, false)

        showChildFragment(DatingFilterFragment())
        setupModeSwitch()
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)

            behavior.peekHeight = 2100

            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            behavior.skipCollapsed = false

            it.setBackgroundResource(R.drawable.bottom_sheet_bg)
        }
    }


    private fun setupModeSwitch() {
        binding.btnDating.setOnClickListener {
            binding.motionNav.transitionToStart()
            binding.btnDating.setTextColor(Color.WHITE)
            binding.btnFriend.setTextColor(Color.GRAY)
            showChildFragment(DatingFilterFragment())
        }

        binding.btnFriend.setOnClickListener {
            binding.motionNav.transitionToEnd()
            binding.btnFriend.setTextColor(Color.WHITE)
            binding.btnDating.setTextColor(Color.GRAY)
            showChildFragment(FriendFilterFragment())
        }
    }

    private fun showChildFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.filterFragmentContainer, fragment)
            .commit()
    }
}


