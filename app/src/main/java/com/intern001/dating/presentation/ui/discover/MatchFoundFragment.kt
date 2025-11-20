package com.intern001.dating.presentation.ui.discover

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.bumptech.glide.Glide
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentMatchFoundBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

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

        matchedUserPhotoUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(binding.ivMatchedUserPhoto)
        }

        currentUserPhotoUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(binding.ivCurrentUserPhoto)
        }
    }

    private fun startMatchAnimation() {
        // Hide elements initially
        binding.ivMatchIcon.alpha = 0f
        binding.ivMatchIcon.scaleX = 0f
        binding.ivMatchIcon.scaleY = 0f
        binding.tvMatchMessage.alpha = 0f
        binding.ivCurrentUserPhoto.alpha = 0f
        binding.ivMatchedUserPhoto.alpha = 0f
        binding.tvMatchedUserName.alpha = 0f

        // Animate match icon
        val iconScaleX = ObjectAnimator.ofFloat(binding.ivMatchIcon, View.SCALE_X, 0f, 1.2f, 1f)
        val iconScaleY = ObjectAnimator.ofFloat(binding.ivMatchIcon, View.SCALE_Y, 0f, 1.2f, 1f)
        val iconAlpha = ObjectAnimator.ofFloat(binding.ivMatchIcon, View.ALPHA, 0f, 1f)

        val iconAnimator = AnimatorSet().apply {
            playTogether(iconScaleX, iconScaleY, iconAlpha)
            duration = 600
            interpolator = OvershootInterpolator()
        }

        // Animate message
        val messageAlpha = ObjectAnimator.ofFloat(binding.tvMatchMessage, View.ALPHA, 0f, 1f)
        messageAlpha.duration = 400
        messageAlpha.startDelay = 300

        // Animate photos from sides
        val currentPhotoTransX = ObjectAnimator.ofFloat(binding.ivCurrentUserPhoto, View.TRANSLATION_X, -300f, 0f)
        val currentPhotoAlpha = ObjectAnimator.ofFloat(binding.ivCurrentUserPhoto, View.ALPHA, 0f, 1f)

        val matchedPhotoTransX = ObjectAnimator.ofFloat(binding.ivMatchedUserPhoto, View.TRANSLATION_X, 300f, 0f)
        val matchedPhotoAlpha = ObjectAnimator.ofFloat(binding.ivMatchedUserPhoto, View.ALPHA, 0f, 1f)

        val photosAnimator = AnimatorSet().apply {
            playTogether(
                currentPhotoTransX,
                currentPhotoAlpha,
                matchedPhotoTransX,
                matchedPhotoAlpha,
            )
            duration = 500
            startDelay = 600
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Animate name
        val nameAlpha = ObjectAnimator.ofFloat(binding.tvMatchedUserName, View.ALPHA, 0f, 1f)
        nameAlpha.duration = 400
        nameAlpha.startDelay = 1100

        // Play all animations
        AnimatorSet().apply {
            playSequentially(iconAnimator, messageAlpha, photosAnimator, nameAlpha)
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
