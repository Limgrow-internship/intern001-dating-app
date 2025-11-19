package com.intern001.dating.presentation.ui.profile.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intern001.dating.databinding.FragmentEditProfileBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment

class EditProfileFragment : BaseFragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
//    private val viewModel: EditProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
