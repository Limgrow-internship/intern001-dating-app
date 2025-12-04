package com.intern001.dating.presentation.ui.onboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.intern001.dating.MainActivity
import com.intern001.dating.databinding.FragmentOnboardThirdBinding // ViewBinding import
import com.intern001.dating.presentation.common.ads.AdManager
import com.intern001.dating.presentation.common.ads.NativeAdHelper
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardThirdFragment : BaseFragment() {
    private var _binding: FragmentOnboardThirdBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentOnboardThirdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? MainActivity)?.hideBottomNavigation(true)

        val adContainer = binding.grayBox

        // Only show ads if user hasn't purchased "no ads"
        if (!viewModel.hasActiveSubscription()) {
            NativeAdHelper.bindNativeAdSmall(
                requireContext(),
                adContainer,
                AdManager.nativeAdSmall,
            )
        } else {
            adContainer.visibility = View.GONE
        }

        binding.btnContinue.setOnClickListener {
            // Always go to language selection after onboarding
            findNavController().navigate(
                com.intern001.dating.R.id.action_onboard3_to_language,
            )
        }
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.hideBottomNavigation(true)
        _binding = null
        super.onDestroyView()
    }
}
