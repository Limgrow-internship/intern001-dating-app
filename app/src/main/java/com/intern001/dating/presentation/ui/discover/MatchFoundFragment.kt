package com.intern001.dating.presentation.ui.discover

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentMatchFoundBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.BlurTransformation

@AndroidEntryPoint
class MatchFoundFragment : BaseFragment() {

    private var _binding: FragmentMatchFoundBinding? = null
    private val binding get() = _binding!!

    private var matchId: String? = null
    private var matchedUserId: String? = null
    private var matchedUserName: String? = null
    private var matchedUserPhotoUrl: String? = null
    private var currentUserPhotoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            matchId = it.getString(ARG_MATCH_ID)
            matchedUserId = it.getString(ARG_MATCHED_USER_ID)
            matchedUserName = it.getString(ARG_MATCHED_USER_NAME)
            matchedUserPhotoUrl = it.getString(ARG_MATCHED_USER_PHOTO)
            currentUserPhotoUrl = it.getString(ARG_CURRENT_USER_PHOTO)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMatchFoundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide bottom navigation
        (activity as? MainActivity)?.hideBottomNavigation(true)

        setupListeners()
        loadMatchData()
        startMatchAnimation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        binding.btnSendMessage.setOnClickListener {
            // Navigate to chat with matched user
            matchedUserId?.let { userId ->
                navigateToChatWithUser(userId)
            }
        }

        binding.btnKeepSwiping.setOnClickListener {
            // Navigate back to discover screen
            navigateUp()
        }
    }

    private fun loadMatchData() {
        matchedUserName?.let {
            binding.tvMatchedUserName.text = it
        }

        // Load blurred background photo
        matchedUserPhotoUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                .centerCrop()
                .into(binding.ivBackgroundPhoto)

            // Also apply RenderEffect for API 31+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                binding.ivBackgroundPhoto.setRenderEffect(
                    RenderEffect.createBlurEffect(30f, 30f, Shader.TileMode.CLAMP)
                )
            }
        }
    }

    private fun startMatchAnimation() {
        // Hide elements initially
        binding.ivMatchIcon.alpha = 0f
        binding.ivMatchIcon.scaleX = 0f
        binding.ivMatchIcon.scaleY = 0f
        binding.tvMatchMessage.alpha = 0f
        binding.tvMatchTitle.alpha = 0f
        binding.buttonsContainer.alpha = 0f

        // Animate match icon with bounce effect
        val iconScaleX = ObjectAnimator.ofFloat(binding.ivMatchIcon, View.SCALE_X, 0f, 1.3f, 1f)
        val iconScaleY = ObjectAnimator.ofFloat(binding.ivMatchIcon, View.SCALE_Y, 0f, 1.3f, 1f)
        val iconAlpha = ObjectAnimator.ofFloat(binding.ivMatchIcon, View.ALPHA, 0f, 1f)

        val iconAnimator = AnimatorSet().apply {
            playTogether(iconScaleX, iconScaleY, iconAlpha)
            duration = 700
            interpolator = OvershootInterpolator(2f)
        }

        // Animate title
        val titleAlpha = ObjectAnimator.ofFloat(binding.tvMatchTitle, View.ALPHA, 0f, 1f)
        val titleTransY = ObjectAnimator.ofFloat(binding.tvMatchTitle, View.TRANSLATION_Y, 50f, 0f)
        val titleAnimator = AnimatorSet().apply {
            playTogether(titleAlpha, titleTransY)
            duration = 400
        }

        // Animate message
        val messageAlpha = ObjectAnimator.ofFloat(binding.tvMatchMessage, View.ALPHA, 0f, 1f)
        messageAlpha.duration = 400

        // Animate buttons
        val buttonsAlpha = ObjectAnimator.ofFloat(binding.buttonsContainer, View.ALPHA, 0f, 1f)
        val buttonsTransY = ObjectAnimator.ofFloat(binding.buttonsContainer, View.TRANSLATION_Y, 100f, 0f)
        val buttonsAnimator = AnimatorSet().apply {
            playTogether(buttonsAlpha, buttonsTransY)
            duration = 500
        }

        // Play all animations sequentially
        AnimatorSet().apply {
            playSequentially(iconAnimator, titleAnimator, messageAlpha, buttonsAnimator)
            start()
        }
    }

    private fun navigateToChatWithUser(userId: String) {
        // TODO: Navigate to chat screen with userId
        // navigationManager.navigateToDynamicRoute(navController, "chat/$userId")
    }

    companion object {
        private const val ARG_MATCH_ID = "match_id"
        private const val ARG_MATCHED_USER_ID = "matched_user_id"
        private const val ARG_MATCHED_USER_NAME = "matched_user_name"
        private const val ARG_MATCHED_USER_PHOTO = "matched_user_photo"
        private const val ARG_CURRENT_USER_PHOTO = "current_user_photo"

        fun newInstance(
            matchId: String,
            matchedUserId: String,
            matchedUserName: String,
            matchedUserPhotoUrl: String?,
            currentUserPhotoUrl: String?,
        ) = MatchFoundFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MATCH_ID, matchId)
                putString(ARG_MATCHED_USER_ID, matchedUserId)
                putString(ARG_MATCHED_USER_NAME, matchedUserName)
                putString(ARG_MATCHED_USER_PHOTO, matchedUserPhotoUrl)
                putString(ARG_CURRENT_USER_PHOTO, currentUserPhotoUrl)
            }
        }
    }
}
