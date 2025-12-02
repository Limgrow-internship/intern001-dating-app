package com.intern001.dating.presentation.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.intern001.dating.databinding.FragmentLikedYouBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.home.LikedYouViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.intern001.dating.R

@AndroidEntryPoint
class LikedYouFragment : BaseFragment() {

    private var _binding: FragmentLikedYouBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LikedYouViewModel by viewModels()

    private lateinit var adapter: LikedYouAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLikedYouBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        observeViewModel()
        viewModel.loadUsersWhoLikedMe()
    }

    private fun setupRecycler() {
        adapter = LikedYouAdapter { user ->
            findNavController().navigate(
                R.id.action_home_to_datingMode,
                bundleOf("targetUserId" to user.userId)
            )
        }

        binding.rvLikedYou.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LikedYouFragment.adapter
        }

        binding.rvLikedYou.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.likedYouState.collect { state ->

                    when (state) {

                        is LikedYouViewModel.UiState.Idle -> {
                            // nothing
                        }

                        is LikedYouViewModel.UiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is LikedYouViewModel.UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            adapter.updateData(state.data)
                        }

                        is LikedYouViewModel.UiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.txtError.text = state.message
                            binding.txtError.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
