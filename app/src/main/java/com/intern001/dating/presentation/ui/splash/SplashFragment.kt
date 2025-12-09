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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

        viewModel.prefetchLanguages()

        if (viewModel.hasActiveSubscription()) {
            lifecycleScope.launch {
                delay(500)
                if (isAdded && view != null) {
                    navigateToNextScreen()
                }
            }
        } else {
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
    }

    private suspend fun navigateToNextScreen() {
        val isLoggedIn = authRepository.isLoggedIn()

        // Prefetch data if logged in (for faster loading later)
        prefetchUserDataIfNeeded(isLoggedIn)

        // Always go to onboarding from splash
        navController.navigate(R.id.action_splash_to_onboard1)
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
