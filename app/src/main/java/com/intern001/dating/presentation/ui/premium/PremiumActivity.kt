package com.intern001.dating.presentation.ui.premium

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.intern001.dating.data.billing.BillingConfig
import com.intern001.dating.data.billing.BillingManager
import com.intern001.dating.databinding.ActivityPremiumBinding
import com.intern001.dating.presentation.common.state.PurchaseState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PremiumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPremiumBinding
    private val viewModel: PremiumViewModel by viewModels()

    @Inject
    lateinit var billingManager: BillingManager

    private var selectedTier: TierType = TierType.BASIC
    private var selectedPlan: PlanType = PlanType.MONTHLY
    private var productsMap: Map<String, ProductDetails> = emptyMap()
    private val tierPricingMap = mutableMapOf<TierType, TierPricing>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupListeners()
        observeProducts()
        observePurchaseState()
    }

    private fun setupViewPager() {
        val adapter = PremiumPagerAdapter(this) { tierType, planType ->
            selectedTier = tierType
            selectedPlan = planType
            handlePurchase()
        }

        binding.viewPager.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun observeProducts() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subscriptionProducts.collect { products ->
                    if (products.isNotEmpty()) {
                        productsMap = products.associateBy { it.productId }
                        updateTierPricing()
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observePurchaseState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purchaseState.collect { state ->
                    when (state) {
                        is PurchaseState.Loading -> {
                            showLoading(true)
                        }
                        is PurchaseState.Purchasing -> {
                            showLoading(true)
                        }
                        is PurchaseState.Purchased -> {
                            showLoading(false)
                            showPurchaseSuccessDialog()
                        }
                        is PurchaseState.Error -> {
                            showLoading(false)
                            Toast.makeText(this@PremiumActivity, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                            viewModel.resetPurchaseState()
                        }
                        is PurchaseState.Canceled -> {
                            showLoading(false)
                            Toast.makeText(this@PremiumActivity, "Purchase canceled", Toast.LENGTH_SHORT).show()
                            viewModel.resetPurchaseState()
                        }
                        is PurchaseState.Pending -> {
                            Toast.makeText(this@PremiumActivity, "Purchase pending...", Toast.LENGTH_SHORT).show()
                        }
                        is PurchaseState.NotPurchased -> {
                            showLoading(false)
                        }
                        is PurchaseState.Idle -> {
                            showLoading(false)
                        }
                    }
                }
            }
        }
    }

    private fun handlePurchase() {
        val productId = getProductId(selectedTier)
        if (productId == null) {
            Toast.makeText(this, "Product not available", Toast.LENGTH_SHORT).show()
            return
        }

        val offerToken = tierPricingMap[selectedTier]?.offerTokenFor(selectedPlan)
        billingManager.launchPurchaseFlow(this, productId, offerToken)
    }

    private fun getProductId(tier: TierType): String? {
        return when (tier) {
            TierType.BASIC -> BillingConfig.Subscriptions.BASIC
            TierType.GOLD -> BillingConfig.Subscriptions.GOLD
            TierType.ELITE -> BillingConfig.Subscriptions.ELITE
        }
    }

    private fun showLoading(show: Boolean) {
        // You can show/hide a loading indicator here if needed
    }

    private fun showPurchaseSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Purchase Successful")
            .setMessage("Thank you for upgrading to ${selectedTier.name}! Enjoy your premium features.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun updateTierPricing() {
        TierType.values().forEach { tier ->
            val productId = getProductId(tier) ?: return@forEach
            val pricing = buildTierPricing(productId)
            tierPricingMap[tier] = pricing
            notifyPriceChange(tier, pricing)
        }
    }

    private fun notifyPriceChange(tier: TierType, pricing: TierPricing) {
        val resultBundle = bundleOf(
            PremiumTierFragment.RESULT_WEEKLY_PRICE to pricing.weeklyPrice,
            PremiumTierFragment.RESULT_MONTHLY_PRICE to pricing.monthlyPrice,
        )
        supportFragmentManager.setFragmentResult(
            PremiumTierFragment.priceResultKey(tier),
            resultBundle,
        )
    }

    private fun buildTierPricing(productId: String): TierPricing {
        val productDetails = productsMap[productId] ?: return TierPricing()
        val weeklyOffer = productDetails.findOfferByTag("weekly")
        val monthlyOffer = productDetails.findOfferByTag("monthly")
        val fallbackOffer = productDetails.subscriptionOfferDetails?.firstOrNull()

        return TierPricing(
            weeklyPrice = weeklyOffer?.firstFormattedPrice() ?: fallbackOffer?.firstFormattedPrice(),
            monthlyPrice = monthlyOffer?.firstFormattedPrice() ?: fallbackOffer?.firstFormattedPrice(),
            weeklyOfferToken = weeklyOffer?.offerToken ?: fallbackOffer?.offerToken,
            monthlyOfferToken = monthlyOffer?.offerToken ?: fallbackOffer?.offerToken,
        )
    }

    private fun ProductDetails.findOfferByTag(tag: String): SubscriptionOfferDetails? {
        val offers = subscriptionOfferDetails ?: return null
        return offers.firstOrNull { offer ->
            offer.offerTags.any { it.equals(tag, ignoreCase = true) }
        }
    }

    private fun SubscriptionOfferDetails.firstFormattedPrice(): String? {
        return pricingPhases.pricingPhaseList.firstOrNull()?.formattedPrice
    }

    private data class TierPricing(
        val weeklyPrice: String? = null,
        val monthlyPrice: String? = null,
        val weeklyOfferToken: String? = null,
        val monthlyOfferToken: String? = null,
    ) {
        fun offerTokenFor(planType: PlanType): String? {
            return when (planType) {
                PlanType.WEEKLY -> weeklyOfferToken ?: monthlyOfferToken
                PlanType.MONTHLY -> monthlyOfferToken ?: weeklyOfferToken
            }
        }
    }
}
