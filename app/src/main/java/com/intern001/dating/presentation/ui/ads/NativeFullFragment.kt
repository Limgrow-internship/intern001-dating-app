package com.intern001.dating.presentation.ui.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentNativeFullBinding
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

    @Inject
    lateinit var languageRepository: LanguageRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentNativeFullBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adContainer = binding.nativeAdContainer

        NativeAdHelper.bindNativeAdFull(
            requireContext(),
            adContainer,
            AdManager.nativeAdFull,
        ) { adView, ad ->

            val navigateToLanguage: () -> Unit = {
                lifecycleScope.launch {
                    languageRepository.prefetchLanguages()
                    findNavController().navigate(R.id.action_nativeFull_to_language)
                }
            }

            val btnCloseTop = adView.findViewById<ImageView>(R.id.ad_close_1)
            btnCloseTop?.setOnClickListener { navigateToLanguage() }

            val btnCloseBottom = adView.findViewById<ImageView>(R.id.ad_close)
            btnCloseBottom?.setOnClickListener { navigateToLanguage() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.hideBottomNavigation(true)
        _binding = null
    }
}
