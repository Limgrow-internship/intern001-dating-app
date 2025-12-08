package com.intern001.dating.presentation.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.databinding.FragmentProfileBinding
import com.intern001.dating.domain.model.UpdateProfile
import com.intern001.dating.domain.model.UserProfile
import com.intern001.dating.presentation.common.ads.AdManager
import com.intern001.dating.presentation.common.ads.NativeAdHelper
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.common.viewmodel.ProfileViewModel
import com.intern001.dating.presentation.ui.premium.PremiumActivity
import com.intern001.dating.presentation.ui.profile.edit.EditProfileViewModel
import com.intern001.dating.presentation.ui.profile.edit.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val viewModel1: EditProfileViewModel by viewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        val adContainer = binding.grayBox

        observeUserProfile()
        loadUserProfile()

        // Only show ads if user hasn't purchased "no ads"
        if (!viewModel.hasActiveSubscription()) {
            context?.let { ctx ->
                NativeAdHelper.bindNativeAdSmall(
                    ctx,
                    adContainer,
                    AdManager.nativeAdSmall,
                )
            }
        } else {
            adContainer.visibility = View.GONE
        }

        val btnVerifyProfile = view.findViewById<LinearLayout>(R.id.btnVerifyProfile)
        btnVerifyProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_verifyAccount)
        }

        binding.btnNotificationSetting.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_notificationSetting)
        }

        binding.btnContinuePremium.setOnClickListener {
            navigateToPremium()
        }

        binding.btnLogout.setOnClickListener {
            showLogOutBottomSheet()
        }

        binding.btnProfile.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            startActivity(intent)
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

    private fun loadUserProfile() {
        val firstName = tokenManager.getFirstName()
        val lastName = tokenManager.getLastName()
        val gender = tokenManager.getGender()
        val mode = tokenManager.getMode()
        val avatar = tokenManager.getAvatar()
        val picture = tokenManager.getPicture()

        if (firstName != null || lastName != null || gender != null || mode != null) {
            val profile = UpdateProfile.fromLocal(
                firstName = firstName,
                lastName = lastName,
                gender = gender,
                mode = mode,
                avatar = avatar ?: picture,
            )
            bindProfileData(profile)
        }

        viewModel1.getUserProfile()
    }

    private fun observeUserProfile() {
        lifecycleScope.launch {
            viewModel1.userProfileState.collectLatest { state ->
                when (state) {
                    is EditProfileViewModel.UiState.Success<*> -> {
                        val profile = state.data as UserProfile

                        val finalAvatar = when {
                            !profile.avatar.isNullOrBlank() -> profile.avatar
                            !profile.photos.firstOrNull()?.url.isNullOrBlank() -> profile.photos.first().url
                            else -> ""
                        }

                        bindProfileData(profile.copy(avatar = finalAvatar))

                        val picture = profile.photos.firstOrNull()?.url ?: ""

                        tokenManager.saveUserProfile(
                            firstName = profile.firstName ?: "",
                            lastName = profile.lastName ?: "",
                            gender = profile.gender ?: "",
                            mode = profile.mode ?: "",
                            avatar = profile.avatar ?: "",
                            picture = picture,
                        )
                    }

                    is EditProfileViewModel.UiState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun bindProfileData(profile: UserProfile) {
        val b = _binding ?: return

        val finalAvatar = profile.avatar

        if (!finalAvatar.isNullOrEmpty()) {
            Glide.with(b.avatarImageView.context)
                .load(finalAvatar)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(b.avatarImageView)
        } else {
            b.avatarImageView.setImageResource(R.drawable.ic_person)
        }

        val fullName = listOfNotNull(profile.firstName, profile.lastName).joinToString(" ")
        b.tvName.text = fullName
        b.tvGender.text = profile.gender ?: getString(R.string.profile_gender_placeholder)
        b.tvMode.text = profile.mode ?: getString(R.string.dating)
    }

    private fun showLogOutBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_log_out, null)
        dialog.setContentView(view)

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        val btnCancel = view.findViewById<ImageView>(R.id.btnCancel)
        val btnConfirmLogout = view.findViewById<TextView>(R.id.btnLogout)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmLogout.setOnClickListener {
            dialog.dismiss()
            performLogout()
        }

        dialog.show()
    }

    private fun performLogout() {
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

    private fun navigateToPremium() {
        val intent = Intent(requireContext(), PremiumActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
