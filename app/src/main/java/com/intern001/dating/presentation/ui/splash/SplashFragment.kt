package com.intern001.dating.presentation.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@AndroidEntryPoint
class SplashFragment : BaseFragment() {

    private val viewModel: SplashViewModel by viewModels()

    @Inject
    lateinit var appPreferencesManager: AppPreferencesManager

    @Inject
    lateinit var authRepository: AuthRepository

    private var prefetchJob: Job? = null

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
            // Use lifecycleScope instead of viewLifecycleOwner to avoid crash when view is destroyed
            lifecycleScope.launch {
                delay(1500)
                // Check if fragment is still attached and view exists before navigation
                if (isAdded && view != null) {
                    navigateToNextScreen()
                }
            }
        }
    }

    private suspend fun navigateToNextScreen() {
        val onboardingCompleted = appPreferencesManager.isOnboardingCompleted()
        val isLoggedIn = authRepository.isLoggedIn()
        val hasPurchasedNoAds = viewModel.hasActiveSubscription()

        prefetchUserDataIfNeeded(isLoggedIn)

        when {
            !onboardingCompleted -> {
                // User hasn't completed onboarding -> go to onboarding
                navController.navigate(R.id.action_splash_to_onboard1)
            }
            hasPurchasedNoAds && !isLoggedIn -> {
                // User purchased "no ads" but not logged in -> go to login (skip ads)
                navController.navigate(R.id.action_splash_to_login)
            }
            hasPurchasedNoAds && isLoggedIn -> {
                // User purchased "no ads" and logged in -> go to home (skip ads)
                navController.navigate(R.id.action_splash_to_home)
            }
            !isLoggedIn -> {
                // User hasn't purchased and not logged in -> go to login
                navController.navigate(R.id.action_splash_to_login)
            }
            else -> {
                // User hasn't purchased but logged in -> show full ad
                navController.navigate(R.id.action_splash_to_nativeAd)
            }
        }
    }

    private fun prefetchUserDataIfNeeded(isLoggedIn: Boolean) {
        if (!isLoggedIn || prefetchJob?.isActive == true) return
        prefetchJob =
            lifecycleScope.launch {
                withTimeoutOrNull(PREFETCH_TIMEOUT_MS) {
                    viewModel.prefetchHomeData()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.hideBottomNavigation(true)
        prefetchJob?.cancel()
    }

    companion object {
        private const val PREFETCH_TIMEOUT_MS = 3_000L
    }
}
