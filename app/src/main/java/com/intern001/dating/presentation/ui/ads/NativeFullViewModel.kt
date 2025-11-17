package com.intern001.dating.presentation.ui.ads

import com.intern001.dating.data.billing.BillingManager
import com.intern001.dating.presentation.common.viewmodel.BaseViewModelAds
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NativeFullViewModel @Inject constructor(
    billingManager: BillingManager,
) : BaseViewModelAds(billingManager)
