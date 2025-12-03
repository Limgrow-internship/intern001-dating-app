package com.intern001.dating.presentation.ui.verify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentVerificationSuccessBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment

class VerificationSuccessFragment : BaseFragment() {
    private var _binding: FragmentVerificationSuccessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentVerificationSuccessBinding.inflate(inflater, container, false)

        binding.btnDone.setOnClickListener {
            findNavController().navigate(
                com.intern001.dating.R.id.action_verificationSuccessFragment_to_home,
            )
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
