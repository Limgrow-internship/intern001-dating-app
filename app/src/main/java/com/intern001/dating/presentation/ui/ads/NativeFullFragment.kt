package com.intern001.dating.presentation.ui.ads

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.intern001.dating.R
import com.intern001.dating.data.local.AppPreferencesManager
import com.intern001.dating.databinding.FragmentNativeFullBinding
import com.intern001.dating.domain.repository.AuthRepository
import com.intern001.dating.domain.repository.LanguageRepository
import com.intern001.dating.presentation.common.ads.AdManager
import com.intern001.dating.presentation.common.ads.NativeAdHelper
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NativeFullFragment : BaseFragment() {
    private var _binding: FragmentNativeFullBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NativeFullViewModel by viewModels()

    @Inject
    lateinit var languageRepository: LanguageRepository

    @Inject
    lateinit var appPreferencesManager: AppPreferencesManager

    @Inject
    lateinit var authRepository: AuthRepository

    private val autoCloseHandler = Handler(Looper.getMainLooper())
    private var autoCloseRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentNativeFullBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navigateToNextScreen: () -> Unit = {
            lifecycleScope.launch {
                val languageSelected = appPreferencesManager.isLanguageSelected()
                val isLoggedIn = authRepository.isLoggedIn()

                when {
                    !languageSelected -> {
                        // Prefetch languages and navigate to language selection
                        languageRepository.prefetchLanguages()
                        findNavController().navigate(R.id.action_nativeFull_to_language)
                    }
                    !isLoggedIn -> {
                        // Navigate to login
                        findNavController().navigate(R.id.action_nativeFull_to_login)
                    }
                    else -> {
                        // User has selected language and logged in -> go to home
                        findNavController().navigate(R.id.action_nativeFull_to_home)
                    }
                }
            }
        }

        // If user has purchased "no ads", skip ad and navigate immediately
        if (viewModel.hasActiveSubscription()) {
            navigateToNextScreen()
        } else {
            // Show full screen ad
            val adContainer = binding.nativeAdContainer
            NativeAdHelper.bindNativeAdFull(
                requireContext(),
                adContainer,
                AdManager.nativeAdFull,
            ) { adView, ad ->
                // Auto close after 2 seconds
                autoCloseRunnable = Runnable {
                    navigateToNextScreen()
                }
                autoCloseHandler.postDelayed(autoCloseRunnable!!, 2000)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoCloseRunnable?.let { autoCloseHandler.removeCallbacks(it) }
        _binding = null
    }
}
