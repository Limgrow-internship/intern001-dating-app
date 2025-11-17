package com.intern001.dating.presentation.common.viewmodel

import com.intern001.dating.data.billing.BillingManager
import com.intern001.dating.presentation.common.state.PurchaseState
import kotlinx.coroutines.flow.StateFlow

open class BaseViewModelAds(
    private val billingManager: BillingManager,
) : BaseViewModel() {

    val purchaseState: StateFlow<PurchaseState> = billingManager.purchaseState

    fun hasActiveSubscription(): Boolean {
        return billingManager.hasActiveSubscription()
    }
}
