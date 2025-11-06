package com.intern001.dating.presentation.ui.onboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.presentation.common.ads.AdManager
import com.intern001.dating.presentation.common.ads.NativeAdHelper
import com.intern001.dating.presentation.common.viewmodel.BaseFragment

class OnboardFirstFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboard_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? MainActivity)?.hideBottomNavigation(true)
        val adContainer = view.findViewById<FrameLayout>(R.id.grayBox)
        NativeAdHelper.bindNativeAd(
            requireContext(),
            adContainer,
            AdManager.nativeAdSmall,
            R.layout.layout_native_ad_small,
        )
        view.findViewById<LinearLayout>(R.id.btnContinue).setOnClickListener {
            findNavController().navigate(R.id.action_onboardFirst_to_onboardSecond)
        }
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.hideBottomNavigation(true)
        super.onDestroyView()
    }
}
