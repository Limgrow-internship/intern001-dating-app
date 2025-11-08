package com.intern001.dating.presentation.ui.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentNativeFullBinding
import com.intern001.dating.domain.repository.AuthRepository
import com.intern001.dating.presentation.common.ads.AdManager
import com.intern001.dating.presentation.common.ads.NativeAdHelper
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NativeFullFragment : BaseFragment() {
    private var _binding: FragmentNativeFullBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var authRepository: AuthRepository

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

            val btnCloseTop = adView.findViewById<ImageView>(R.id.ad_close_1)
            btnCloseTop?.setOnClickListener {
                navigateToNextScreen()
            }
            val btnCloseBottom = adView.findViewById<ImageView>(R.id.ad_close)
            btnCloseBottom?.setOnClickListener {
                navigateToNextScreen()
            }
        }
    }

    private fun navigateToNextScreen() {
        if (authRepository.isLoggedIn()) {
            // User đã đăng nhập, chuyển đến home screen
            findNavController().navigate(R.id.action_nativeFull_to_home)
        } else {
            // User chưa đăng nhập, chuyển đến login screen
            findNavController().navigate(R.id.action_nativeFull_to_login)
        }
    }

    override fun onDestroyView() {
        // Don't manually control bottom nav visibility - let destination listener handle it
        _binding = null
        super.onDestroyView()
    }
}
