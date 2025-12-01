package com.intern001.dating.presentation.ui.premium

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
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
        // Map tier and plan to product ID
        val productId = getProductId(selectedTier, selectedPlan)
        if (productId == null) {
            Toast.makeText(this, "Product not available", Toast.LENGTH_SHORT).show()
            return
        }

        billingManager.launchPurchaseFlow(this, productId)
    }

    private fun getProductId(tier: TierType, plan: PlanType): String? {
        // Map combinations to product IDs
        // This is a simple mapping - adjust based on your billing configuration
        return when (plan) {
            PlanType.WEEKLY -> BillingConfig.Subscriptions.WEEKLY
            PlanType.MONTHLY -> BillingConfig.Subscriptions.MONTHLY
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
}
