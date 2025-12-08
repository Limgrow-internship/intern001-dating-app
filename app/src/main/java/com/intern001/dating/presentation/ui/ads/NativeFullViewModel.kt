package com.intern001.dating.presentation.ui.ads

import com.intern001.dating.data.billing.BillingManager
import com.intern001.dating.domain.usecase.PrefetchHomeDataUseCase
import com.intern001.dating.presentation.common.viewmodel.BaseViewModelAds
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class NativeFullViewModel @Inject constructor(
    billingManager: BillingManager,
) : BaseViewModelAds(billingManager) {

    @Inject
    lateinit var prefetchHomeDataUseCase: PrefetchHomeDataUseCase

    suspend fun prefetchHomeData() = withContext(Dispatchers.IO) {
        runCatching { prefetchHomeDataUseCase() }
    }
}
