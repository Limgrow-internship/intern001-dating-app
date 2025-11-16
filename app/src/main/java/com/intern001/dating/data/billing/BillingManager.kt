@file:Suppress("ktlint:standard:import-ordering")

package com.intern001.dating.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.intern001.dating.presentation.common.state.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var billingClient: BillingClient? = null

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Loading)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _subscriptionProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val subscriptionProducts: StateFlow<List<ProductDetails>> = _subscriptionProducts.asStateFlow()

    private var isInitialized = false

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                scope.launch {
                    handlePurchase(purchase)
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _purchaseState.value = PurchaseState.Canceled
        } else {
            if (_purchaseState.value is PurchaseState.Loading ||
                _purchaseState.value is PurchaseState.Purchasing
            ) {
                queryPurchases()
            }
        }
    }

    init {
        initialize()
    }

    private fun initialize() {
        if (isInitialized) {
            return
        }

        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isInitialized = true
                    queryInAppProducts()
                    queryPurchases()
                } else {
                    Log.e(TAG, "BillingClient setup failed: ${billingResult.debugMessage}")
                    _purchaseState.value = PurchaseState.NotPurchased
                }
            }

            override fun onBillingServiceDisconnected() {
                isInitialized = false
                scope.launch {
                    kotlinx.coroutines.delay(BillingConfig.RetryConfig.RECONNECT_DELAY_MS)
                    if (!isInitialized) {
                        initialize()
                    }
                }
            }
        })
    }

    private fun queryInAppProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BillingConfig.InAppProducts.getNoAdsProductId())
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val products = queryProductDetailsResult.productDetailsList
                _subscriptionProducts.value = products

                if (products.isEmpty()) {
                    Log.w(TAG, "No products found for ${BillingConfig.InAppProducts.getNoAdsProductId()}")
                }
            } else {
                Log.e(TAG, "Query products failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        _purchaseState.value = PurchaseState.Purchasing(productId)

        if (billingClient?.isReady == false) {
            _purchaseState.value = PurchaseState.Error("Billing service not ready")
            return
        }

        val productDetails = _subscriptionProducts.value.find { it.productId == productId }

        if (productDetails == null) {
            _purchaseState.value = PurchaseState.Error("Product not found")
            return
        }

        val productDetailsParamsList = if (productDetails.productType == BillingClient.ProductType.INAPP) {
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build(),
            )
        } else {
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken == null) {
                _purchaseState.value = PurchaseState.Error("Offer not available")
                return
            }
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build(),
            )
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        scope.launch(Dispatchers.Main) {
            billingClient?.launchBillingFlow(activity, billingFlowParams)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                scope.launch {
                    billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            _purchaseState.value = PurchaseState.Purchased(purchase)
                        } else {
                            _purchaseState.value = PurchaseState.Error("Failed to acknowledge purchase")
                        }
                    }
                }
            } else {
                _purchaseState.value = PurchaseState.Purchased(purchase)
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _purchaseState.value = PurchaseState.Pending
        }
    }

    private fun queryPurchases() {
        if (billingClient?.isReady != true) {
            return
        }

        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient?.queryPurchasesAsync(queryPurchasesParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActivePurchase = purchases.any {
                    it.products.contains(BillingConfig.InAppProducts.getNoAdsProductId()) &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                _purchaseState.value = if (hasActivePurchase) {
                    val activePurchase = purchases.first {
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                    }
                    PurchaseState.Purchased(activePurchase)
                } else {
                    PurchaseState.NotPurchased
                }
            } else {
                Log.e(TAG, "Failed to query purchases: ${billingResult.debugMessage}")
            }
        }
    }

    fun hasActiveSubscription(): Boolean {
        return _purchaseState.value is PurchaseState.Purchased
    }

    fun restorePurchases() {
        queryPurchases()
    }

    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
        isInitialized = false
    }

    companion object {
        private const val TAG = "BillingManager"
    }
}
