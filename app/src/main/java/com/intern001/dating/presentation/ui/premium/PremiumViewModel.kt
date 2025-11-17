package com.intern001.dating.presentation.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.intern001.dating.data.billing.BillingManager
import com.intern001.dating.presentation.common.state.PurchaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager,
) : ViewModel() {

    val subscriptionProducts: StateFlow<List<ProductDetails>> =
        billingManager.subscriptionProducts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    val purchaseState: StateFlow<PurchaseState> =
        billingManager.purchaseState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PurchaseState.Idle,
        )

    fun hasActiveSubscription(): Boolean {
        return billingManager.hasActiveSubscription()
    }

    fun resetPurchaseState() {
        billingManager.resetPurchaseState()
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
