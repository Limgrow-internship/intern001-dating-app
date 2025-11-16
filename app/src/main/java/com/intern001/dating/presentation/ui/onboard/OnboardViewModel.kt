package com.intern001.dating.presentation.ui.onboard

import com.intern001.dating.data.billing.BillingManager
import com.intern001.dating.presentation.common.viewmodel.BaseViewModelAds
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardViewModel @Inject constructor(
    billingManager: BillingManager,
) : BaseViewModelAds(billingManager)
