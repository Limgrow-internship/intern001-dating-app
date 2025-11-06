package com.intern001.dating.presentation.ui.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.presentation.common.ads.AdManager
import com.intern001.dating.presentation.common.ads.NativeAdHelper

class NativeFullFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_native_full, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adContainer = view.findViewById<FrameLayout>(R.id.nativeAdContainer)
        NativeAdHelper.bindNativeAd(
            requireContext(),
            adContainer,
            AdManager.nativeAdFull,
            R.layout.layout_native_ad_full,
        ) { adView, ad ->
            val btnCloseTop = adView.findViewById<ImageView>(R.id.ad_close_1)
            btnCloseTop?.setOnClickListener {
                findNavController().navigate(R.id.homeFragment)
            }
            val btnCloseBottom = adView.findViewById<ImageView>(R.id.ad_close)
            btnCloseBottom?.setOnClickListener {
                findNavController().navigate(R.id.homeFragment)
            }
        }
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.hideBottomNavigation(false)
        super.onDestroyView()
    }
}
