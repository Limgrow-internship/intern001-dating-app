package com.intern001.dating.presentation.ui.premium

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    private var selectedProductId: String? = null
    private var productsMap: Map<String, ProductDetails> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
        observeProducts()
        observePurchaseState()
    }

    private fun setupViews() {
        selectedProductId = BillingConfig.InAppProducts.getNoAdsProductId()
        binding.radio1Month.isChecked = true
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.card1Month.setOnClickListener {
            selectPackage(BillingConfig.InAppProducts.getNoAdsProductId())
        }

        binding.radio1Month.setOnClickListener {
            selectPackage(BillingConfig.InAppProducts.getNoAdsProductId())
        }

        binding.btnSubscribe.setOnClickListener {
            handlePurchase()
        }

        binding.tvTerms.setOnClickListener {
            Toast.makeText(this, "Terms of Service", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeProducts() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subscriptionProducts.collect { products ->
                    if (products.isNotEmpty()) {
                        productsMap = products.associateBy { it.productId }
                        updatePricesFromProducts(products)
                        binding.btnSubscribe.isEnabled = true
                    } else {
                        binding.btnSubscribe.isEnabled = false
                        binding.btnSubscribe.text = "Loading products..."
                    }
                }
            }
        }
    }

    private fun updatePricesFromProducts(products: List<ProductDetails>) {
        products.forEach { product ->
            when (product.productId) {
                BillingConfig.InAppProducts.getNoAdsProductId() -> {
                    // Product found, will update button text below
                }
            }
        }

        selectedProductId?.let { productId ->
            updateSubscribeButtonText(productId)
        }
    }

    private fun observePurchaseState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purchaseState.collect { state ->
                    when (state) {
                        is PurchaseState.Loading -> {
                            binding.btnSubscribe.isEnabled = false
                            binding.btnSubscribe.text = "Processing..."
                            showLoading(true)
                        }
                        is PurchaseState.Purchasing -> {
                            binding.btnSubscribe.isEnabled = false
                            binding.btnSubscribe.text = "Processing..."
                            showLoading(true)
                        }
                        is PurchaseState.Purchased -> {
                            showLoading(false)
                            showPurchaseSuccessDialog()
                        }
                        is PurchaseState.Error -> {
                            showLoading(false)
                            binding.btnSubscribe.isEnabled = true
                            updateSubscribeButtonText(selectedProductId ?: "")
                            Toast.makeText(this@PremiumActivity, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                            viewModel.resetPurchaseState()
                        }
                        is PurchaseState.Canceled -> {
                            showLoading(false)
                            binding.btnSubscribe.isEnabled = true
                            updateSubscribeButtonText(selectedProductId ?: "")
                            Toast.makeText(this@PremiumActivity, "Purchase canceled", Toast.LENGTH_SHORT).show()
                            viewModel.resetPurchaseState()
                        }
                        is PurchaseState.Pending -> {
                            showLoading(false)
                            Toast.makeText(this@PremiumActivity, "Purchase pending...", Toast.LENGTH_SHORT).show()
                        }
                        is PurchaseState.NotPurchased -> {
                            showLoading(false)
                            binding.btnSubscribe.isEnabled = true
                        }
                        is PurchaseState.Idle -> {
                            showLoading(false)
                            binding.btnSubscribe.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun selectPackage(productId: String) {
        selectedProductId = productId

        // Update radio button (only one option)
        binding.radio1Month.isChecked = productId == BillingConfig.InAppProducts.getNoAdsProductId()

        // Update button text
        updateSubscribeButtonText(productId)
    }

    private fun updateSubscribeButtonText(productId: String) {
        val product = productsMap[productId]
        val price = product?.oneTimePurchaseOfferDetails?.formattedPrice ?: "N/A"

        binding.btnSubscribe.text = "Purchase for $price"
    }

    private fun handlePurchase() {
        val productId = selectedProductId
        if (productId == null) {
            Toast.makeText(this, "Please select a package", Toast.LENGTH_SHORT).show()
            return
        }

        // Launch billing flow
        billingManager.launchPurchaseFlow(this, productId)
    }

    private fun showLoading(show: Boolean) {
        // You can add a progress bar to the layout if needed
        if (show) {
            binding.btnSubscribe.alpha = 0.5f
        } else {
            binding.btnSubscribe.alpha = 1.0f
        }
    }

    private fun showPurchaseSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Purchase Successful")
            .setMessage("Thank you for your purchase! All ads have been removed from your account.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
