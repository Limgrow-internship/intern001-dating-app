package com.intern001.dating.presentation.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.data.local.AppPreferencesManager
import com.intern001.dating.domain.repository.AuthRepository
import com.intern001.dating.presentation.common.ads.AdManager
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : BaseFragment() {

    @Inject
    lateinit var appPreferencesManager: AppPreferencesManager

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomNavigation(true)

        AdManager.preloadNativeAds(requireContext()) {
            if (!isAdded || view == null) return@preloadNativeAds

            viewLifecycleOwner.lifecycleScope.launch {
                delay(1500)
                if (!isAdded || view == null) return@launch
                navigateToNextScreen()
            }
        }
    }

    private suspend fun navigateToNextScreen() {
        val onboardingCompleted = appPreferencesManager.isOnboardingCompleted()
        val isLoggedIn = authRepository.isLoggedIn()

        when {
            !onboardingCompleted -> {
                navController.navigate(R.id.action_splash_to_onboard1)
            }
            !isLoggedIn -> {
                navController.navigate(R.id.action_splash_to_login)
            }
            else -> {
                navController.navigate(R.id.action_splash_to_nativeAd)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.hideBottomNavigation(true)
    }
}
