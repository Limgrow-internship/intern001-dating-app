package com.intern001.dating.presentation.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentHomeBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.discover.DiscoverFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show bottom navigation when in HomeFragment
        (activity as? MainActivity)?.hideBottomNavigation(false)

        setupListeners()

        // Show DiscoverFragment by default
        if (savedInstanceState == null) {
            showForYou()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        binding.btnForYou.setOnClickListener {
            showForYou()
        }

        binding.btnLikedYou.setOnClickListener {
            showLikedYou()
        }
    }

    private fun showForYou() {
        // Update tab UI
        updateTabUI(true)

        // Show DiscoverFragment
        childFragmentManager.beginTransaction()
            .replace(R.id.homeContainer, DiscoverFragment())
            .commit()
    }

    private fun showLikedYou() {
        // Update tab UI
        updateTabUI(false)

        // TODO: Show LikedYouFragment
        // For now, show empty fragment
        childFragmentManager.beginTransaction()
            .replace(R.id.homeContainer, LikedYouFragment())
            .commit()
    }

    fun hideTabBar(hide: Boolean) {
        _binding?.tabContainer?.visibility = if (hide) View.GONE else View.VISIBLE
    }

    private fun updateTabUI(isForYouSelected: Boolean) {
        if (isForYouSelected) {
            // For You selected
            binding.btnForYou.apply {
                setBackgroundColor(context.getColor(R.color.bottom_nav_selected))
                setTextColor(context.getColor(R.color.white))
            }
            binding.btnLikedYou.apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setTextColor(context.getColor(R.color.bottom_nav_unselected))
            }
        } else {
            // Liked You selected
            binding.btnForYou.apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setTextColor(context.getColor(R.color.bottom_nav_unselected))
            }
            binding.btnLikedYou.apply {
                setBackgroundColor(context.getColor(R.color.bottom_nav_selected))
                setTextColor(context.getColor(R.color.white))
            }
        }
    }
}
