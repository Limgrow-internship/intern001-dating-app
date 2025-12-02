package com.intern001.dating.presentation.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentPremiumTierBinding

class PremiumTierFragment : Fragment() {

    private var _binding: FragmentPremiumTierBinding? = null
    private val binding get() = _binding!!

    private var tierType: TierType = TierType.BASIC
    private var selectedPlan: PlanType = PlanType.MONTHLY
    private var onUpgradeClickListener: ((TierType, PlanType) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPremiumTierBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tierType = arguments?.getSerializable(ARG_TIER_TYPE) as? TierType ?: TierType.BASIC
        setupTierUI()
        setupFeatures()
        setupListeners()
        updatePlanSelection(selectedPlan)
        updateTierIndicator()
        updatePriceUI(null, null)
        listenForPriceUpdates()
    }

    private fun setupTierUI() {
        when (tierType) {
            TierType.BASIC -> {
                binding.ivTierIcon.setImageResource(R.drawable.ic_basic)
                binding.tvTagline.text = "Perfect for new users starting their journey."
                binding.tvMonthlySave.text = "Save 21.000 VND"
            }
            TierType.GOLD -> {
                binding.ivTierIcon.setImageResource(R.drawable.ic_gold)
                binding.tvTagline.text = "Great if you want faster matches and more control."
                binding.tvMonthlySave.text = "Save 31.000 VND"
            }
            TierType.ELITE -> {
                binding.ivTierIcon.setImageResource(R.drawable.ic_elite)
                binding.tvTagline.text = "The ultimate experience for those who want maximum visibility & connection."
                binding.tvMonthlySave.text = "Save 21.000 VND"
            }
        }
    }

    private fun setupFeatures() {
        val features = getFeatures()

        setupFeatureRow(binding.root.findViewById(R.id.featureMatch), "Match", features[0].first, features[0].second)
        setupFeatureRow(binding.root.findViewById(R.id.featureSendMessage), "Send message", features[1].first, features[1].second)
        setupFeatureRow(binding.root.findViewById(R.id.featureVideoVoice), "Video & voice messages", features[2].first, features[2].second)
        setupFeatureRow(binding.root.findViewById(R.id.featureUnlimitedLike), "Unlimited Like", features[3].first, features[3].second)
        setupFeatureRow(binding.root.findViewById(R.id.featureRewind), "Rewind", features[4].first, features[4].second)
        setupFeatureRow(binding.root.findViewById(R.id.featureSuperLike), "Super Like", features[5].first, features[5].second)
        setupFeatureRow(binding.root.findViewById(R.id.featureHideAds), "Hide Ads", features[6].first, features[6].second)
        setupFeatureRow(binding.root.findViewById(R.id.featureViewList), "View list of user who like you", features[7].first, features[7].second)
        setupFeatureRow(binding.root.findViewById(R.id.featureSendBeforeMatch), "Send message before match", features[8].first, features[8].second)
    }

    private fun setupFeatureRow(view: View?, featureName: String, basicHas: Boolean, freeHas: Boolean) {
        view?.apply {
            findViewById<TextView>(R.id.tvFeatureName).text = featureName

            val basicCheckView = findViewById<ImageView>(R.id.ivBasicCheck)
            basicCheckView.setImageResource(
                if (basicHas) R.drawable.ic_check_yellow else R.drawable.ic_no_check,
            )

            val freeCheckView = findViewById<ImageView>(R.id.ivFreeCheck)
            freeCheckView.setImageResource(
                if (freeHas) R.drawable.ic_check_yellow else R.drawable.ic_no_check,
            )
        }
    }

    private fun getFeatures(): List<Pair<Boolean, Boolean>> {
        // Pair = (Basic có hay không, Free có hay không)
        return when (tierType) {
            TierType.BASIC -> listOf(
                true to true, // Match
                true to true, // Send message
                true to false, // Video & voice
                true to false, // Unlimited Like
                true to false, // Rewind
                true to false, // Super Like
                true to false, // Hide Ads
                false to false, // View list - không có cho cả 2
                false to false, // Send before match - không có cho cả 2
            )
            TierType.GOLD -> listOf(
                true to true, // Match
                true to true, // Send message
                true to false, // Video & voice
                true to false, // Unlimited Like
                true to false, // Rewind
                true to false, // Super Like
                true to false, // Hide Ads
                true to false, // View list - chỉ có Basic, không có Free
                false to false, // Send before match - không có cho cả 2
            )
            TierType.ELITE -> listOf(
                true to true, // Match
                true to true, // Send message
                true to false, // Video & voice
                true to false, // Unlimited Like
                true to false, // Rewind
                true to false, // Super Like
                true to false, // Hide Ads
                true to false, // View list - chỉ có Basic, không có Free
                true to false, // Send before match - chỉ có Basic, không có Free
            )
        }
    }

    private fun setupListeners() {
        binding.cardWeekly.setOnClickListener {
            updatePlanSelection(PlanType.WEEKLY)
        }
        binding.ivWeeklySelect.setOnClickListener {
            updatePlanSelection(PlanType.WEEKLY)
        }

        binding.cardMonthly.setOnClickListener {
            updatePlanSelection(PlanType.MONTHLY)
        }
        binding.ivMonthlySelect.setOnClickListener {
            updatePlanSelection(PlanType.MONTHLY)
        }

        binding.btnUpgrade.setOnClickListener {
            onUpgradeClickListener?.invoke(tierType, selectedPlan)
        }
    }

    private fun updatePlanSelection(plan: PlanType) {
        selectedPlan = plan
        val weeklyIcon =
            if (plan == PlanType.WEEKLY) {
                R.drawable.ic_radio_selected
            } else {
                R.drawable.ic_radio_unselected
            }
        val monthlyIcon =
            if (plan == PlanType.MONTHLY) {
                R.drawable.ic_radio_selected
            } else {
                R.drawable.ic_radio_unselected
            }
        binding.ivWeeklySelect.setImageResource(weeklyIcon)
        binding.ivMonthlySelect.setImageResource(monthlyIcon)
    }

    private fun listenForPriceUpdates() {
        parentFragmentManager.setFragmentResultListener(priceResultKey(tierType), viewLifecycleOwner) { _, bundle ->
            val weeklyPrice = bundle.getString(RESULT_WEEKLY_PRICE)
            val monthlyPrice = bundle.getString(RESULT_MONTHLY_PRICE)
            updatePriceUI(weeklyPrice, monthlyPrice)
        }
    }

    private fun updatePriceUI(weeklyPrice: String?, monthlyPrice: String?) {
        binding.tvWeeklyPrice.text = weeklyPrice ?: "--"
        binding.tvMonthlyPrice.text = monthlyPrice ?: "--"
    }

    private fun updateTierIndicator() {
        val activeWidth = resources.getDimensionPixelSize(R.dimen.premium_indicator_active_width)
        val inactiveWidth = resources.getDimensionPixelSize(R.dimen.premium_indicator_inactive_width)
        val indicatorHeight = resources.getDimensionPixelSize(R.dimen.premium_indicator_height)

        fun View.applyIndicatorState(isActive: Boolean) {
            val params = layoutParams as ViewGroup.MarginLayoutParams
            params.width = if (isActive) activeWidth else inactiveWidth
            params.height = indicatorHeight
            layoutParams = params
            setBackgroundResource(if (isActive) R.drawable.indicator_active else R.drawable.indicator_inactive)
            requestLayout()
        }

        when (tierType) {
            TierType.BASIC -> {
                binding.indicatorWeekly.applyIndicatorState(true)
                binding.indicatorMonthly.applyIndicatorState(false)
                binding.indicatorFutureTier.applyIndicatorState(false)
            }
            TierType.GOLD -> {
                binding.indicatorWeekly.applyIndicatorState(false)
                binding.indicatorMonthly.applyIndicatorState(true)
                binding.indicatorFutureTier.applyIndicatorState(false)
            }
            TierType.ELITE -> {
                binding.indicatorWeekly.applyIndicatorState(false)
                binding.indicatorMonthly.applyIndicatorState(false)
                binding.indicatorFutureTier.applyIndicatorState(true)
            }
        }
    }

    fun setOnUpgradeClickListener(listener: (TierType, PlanType) -> Unit) {
        onUpgradeClickListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TIER_TYPE = "tier_type"
        private const val RESULT_KEY_PREFIX = "premium_tier_price_result_"
        const val RESULT_WEEKLY_PRICE = "result_weekly_price"
        const val RESULT_MONTHLY_PRICE = "result_monthly_price"

        fun newInstance(tierType: TierType): PremiumTierFragment {
            return PremiumTierFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TIER_TYPE, tierType)
                }
            }
        }

        fun priceResultKey(tierType: TierType): String = RESULT_KEY_PREFIX + tierType.name
    }
}

enum class TierType {
    BASIC,
    GOLD,
    ELITE,
}

enum class PlanType {
    WEEKLY,
    MONTHLY,
}
