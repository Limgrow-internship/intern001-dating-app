package com.intern001.dating.presentation.common.state

import com.android.billingclient.api.Purchase

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Loading : PurchaseState()
    object NotPurchased : PurchaseState()
    object Pending : PurchaseState()
    object Canceled : PurchaseState()
    data class Purchased(val purchase: Purchase) : PurchaseState()
    data class Purchasing(val productId: String) : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}
