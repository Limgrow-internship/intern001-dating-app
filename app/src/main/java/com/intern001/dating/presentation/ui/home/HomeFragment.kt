package com.intern001.dating.presentation.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.discover.DiscoverFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private lateinit var btnForYou: MaterialButton
    private lateinit var btnLikedYou: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show bottom navigation when in HomeFragment
        (activity as? MainActivity)?.hideBottomNavigation(false)

        initViews(view)
        setupListeners()

        // Show DiscoverFragment by default
        if (savedInstanceState == null) {
            showForYou()
        }
    }

    private fun initViews(view: View) {
        btnForYou = view.findViewById(R.id.btnForYou)
        btnLikedYou = view.findViewById(R.id.btnLikedYou)
    }

    private fun setupListeners() {
        btnForYou.setOnClickListener {
            showForYou()
        }

        btnLikedYou.setOnClickListener {
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

    private fun updateTabUI(isForYouSelected: Boolean) {
        if (isForYouSelected) {
            // For You selected
            btnForYou.apply {
                setBackgroundColor(context.getColor(R.color.bottom_nav_selected))
                setTextColor(context.getColor(R.color.white))
            }
            btnLikedYou.apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setTextColor(context.getColor(R.color.bottom_nav_unselected))
            }
        } else {
            // Liked You selected
            btnForYou.apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setTextColor(context.getColor(R.color.bottom_nav_unselected))
            }
            btnLikedYou.apply {
                setBackgroundColor(context.getColor(R.color.bottom_nav_selected))
                setTextColor(context.getColor(R.color.white))
            }
        }
    }
}
