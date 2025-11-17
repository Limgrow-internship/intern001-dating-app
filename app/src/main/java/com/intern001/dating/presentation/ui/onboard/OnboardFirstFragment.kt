package com.intern001.dating.presentation.ui.onboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.intern001.dating.databinding.FragmentOnboardFirstBinding
import com.intern001.dating.presentation.common.ads.AdManager
import com.intern001.dating.presentation.common.ads.NativeAdHelper
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardFirstFragment : BaseFragment() {
    private var _binding: FragmentOnboardFirstBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnboardViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentOnboardFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Only show ads if user hasn't purchased "no ads"
        if (!viewModel.hasActiveSubscription()) {
            NativeAdHelper.bindNativeAdSmall(
                requireContext(),
                binding.grayBox,
                AdManager.nativeAdSmall,
            )
        } else {
            binding.grayBox.visibility = View.GONE
        }

        binding.btnContinue.setOnClickListener {
            findNavController().navigate(com.intern001.dating.R.id.action_onboardFirst_to_onboardSecond)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
