package com.intern001.dating.presentation.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.intern001.dating.R
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.databinding.FragmentProfileBinding
import com.intern001.dating.presentation.common.ads.AdManager
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import com.intern001.dating.presentation.common.ads.NativeAdHelper
import com.intern001.dating.presentation.ui.login.LoginActivity
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import com.intern001.dating.MainActivity
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {
    @Inject
    lateinit var tokenManager: TokenManager

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        context?.let { ctx ->
            NativeAdHelper.bindNativeAdSmall(
                ctx,
                binding.adContainer,
                AdManager.nativeAdSmall,
            )
        }

        binding.btnLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                tokenManager.clearTokens()

                AdManager.clear()

                context?.let { ctx ->
                    val intent = Intent(ctx, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    Toast.makeText(ctx, "Logout successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
