package com.intern001.dating.presentation.ui.premium

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PremiumPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val onUpgradeClick: (TierType, PlanType) -> Unit
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val tierType = when (position) {
            0 -> TierType.BASIC
            1 -> TierType.GOLD
            2 -> TierType.ELITE
            else -> TierType.BASIC
        }
        
        val fragment = PremiumTierFragment.newInstance(tierType)
        fragment.setOnUpgradeClickListener(onUpgradeClick)
        return fragment
    }
}

