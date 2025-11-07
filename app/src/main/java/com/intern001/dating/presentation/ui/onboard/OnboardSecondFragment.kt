package com.intern001.dating.presentation.ui.onboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.intern001.dating.MainActivity
import com.intern001.dating.databinding.FragmentOnboardSecondBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment

class OnboardSecondFragment : BaseFragment() {
    private var _binding: FragmentOnboardSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentOnboardSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? MainActivity)?.hideBottomNavigation(true)
        binding.btnContinue.setOnClickListener {
            findNavController().navigate(com.intern001.dating.R.id.action_onboardSecond_to_onboardThird)
        }
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.hideBottomNavigation(true)
        _binding = null
        super.onDestroyView()
    }
}
