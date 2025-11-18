package com.intern001.dating.presentation.ui.verify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentVerifyMainBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment

class VerifyAccountFragment : BaseFragment() {
    private var _binding: FragmentVerifyMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentVerifyMainBinding.inflate(inflater, container, false)

        binding.btnGetStarted.setOnClickListener {
            findNavController().navigate(R.id.action_verifyAccountFragment_to_verifyCameraFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
