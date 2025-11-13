package com.intern001.dating.presentation.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.databinding.FragmentProfileBinding
import com.intern001.dating.presentation.common.ads.AdManager
import com.intern001.dating.presentation.common.ads.NativeAdHelper
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.common.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {
    @Inject
    lateinit var tokenManager: TokenManager

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
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

        val btnDeleteAccount = view.findViewById<LinearLayout>(R.id.btnDeleteAccount)
        btnDeleteAccount.setOnClickListener {
            showDeleteAccountBottomSheet()
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            if (result.isSuccess) {
                showDeleteAccountSuccessSheet()
            } else {
                Toast.makeText(context, "Delete failed!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showDeleteAccountBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_delete_account, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.btnDelete).setOnClickListener {
            dialog.dismiss()
            viewModel.deleteAccount()
        }
        dialog.show()
    }

    private fun showDeleteAccountSuccessSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_delete_success, null)
        dialog.setContentView(view)
        view.findViewById<TextView>(R.id.btnDone).setOnClickListener {
            dialog.dismiss()
            goToLoginScreen()
        }
        dialog.show()
    }

    private fun goToLoginScreen() {
        findNavController().navigate(R.id.action_profile_to_login)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
