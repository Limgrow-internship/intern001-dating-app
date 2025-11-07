package com.intern001.dating.presentation.ui.onboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.intern001.dating.MainActivity
import com.intern001.dating.databinding.FragmentOnboardFirstBinding
import com.intern001.dating.presentation.common.ads.AdManager
import com.intern001.dating.presentation.common.ads.NativeAdHelper
import com.intern001.dating.presentation.common.viewmodel.BaseFragment

class OnboardFirstFragment : BaseFragment() {
    private var _binding: FragmentOnboardFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentOnboardFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? MainActivity)?.hideBottomNavigation(true)

        NativeAdHelper.bindNativeAdSmall(
            requireContext(),
            binding.grayBox,
            AdManager.nativeAdSmall,
        )

        binding.btnContinue.setOnClickListener {
            findNavController().navigate(com.intern001.dating.R.id.action_onboardFirst_to_onboardSecond)
        }
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.hideBottomNavigation(true)
        _binding = null
        super.onDestroyView()
    }
}
