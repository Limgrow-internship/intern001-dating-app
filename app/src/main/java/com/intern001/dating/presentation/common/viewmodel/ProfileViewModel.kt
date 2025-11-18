package com.intern001.dating.presentation.common.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.intern001.dating.data.billing.BillingManager
import com.intern001.dating.domain.usecase.auth.DeleteAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val deleteAccountUseCase: DeleteAccountUseCase,
    billingManager: BillingManager,
) : BaseViewModelAds(billingManager) {

    val deleteResult = MutableLiveData<Result<Unit>>()

    fun deleteAccount() {
        viewModelScope.launch {
            val result = deleteAccountUseCase()
            deleteResult.postValue(result)
        }
    }
}
