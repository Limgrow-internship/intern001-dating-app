package com.intern001.dating.presentation.ui.splash

import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.billing.BillingManager
import com.intern001.dating.domain.usecase.PrefetchHomeDataUseCase
import com.intern001.dating.domain.usecase.auth.PrefetchLanguagesUseCase
import com.intern001.dating.presentation.common.viewmodel.BaseViewModelAds
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    billingManager: BillingManager,
    private val prefetchHomeDataUseCase: PrefetchHomeDataUseCase,
    private val prefetchLanguagesUseCase: PrefetchLanguagesUseCase,
) : BaseViewModelAds(billingManager) {

    suspend fun prefetchHomeData() {
        runCatching { prefetchHomeDataUseCase() }
    }

    fun prefetchLanguages() {
        viewModelScope.launch {
            runCatching { prefetchLanguagesUseCase() }
        }
    }
}
