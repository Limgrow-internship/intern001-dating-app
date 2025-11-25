package com.intern001.dating.presentation.ui.discover

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.intern001.dating.R
import com.intern001.dating.databinding.DialogMatchOverlayBinding
import jp.wasabeef.glide.transformations.BlurTransformation

class MatchOverlayDialog : DialogFragment() {

    private var _binding: DialogMatchOverlayBinding? = null
    private val binding get() = _binding!!

    private var matchedUserPhotoUrl: String? = null
    private var matchedUserId: String? = null
    private val dismissHandler = Handler(Looper.getMainLooper())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
            // Make dialog window transparent and allow seeing through
            decorView.setBackgroundColor(Color.TRANSPARENT)
            // Make window transparent
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
            // Set dim amount to 0 to make it fully transparent
            setDimAmount(0f)
        }
        // Prevent dialog from being dismissed by clicking outside or back button
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogMatchOverlayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            matchedUserPhotoUrl = it.getString(ARG_MATCHED_USER_PHOTO)
            matchedUserId = it.getString(ARG_MATCHED_USER_ID)
        }

        // Make root view clickable to prevent clicks passing through
        view.setOnClickListener {
            // Do nothing, just prevent clicks from passing through
        }

        loadMatchData()
        startMatchAnimation()
        
        // Auto dismiss after 3 seconds
        dismissHandler.postDelayed({
            if (isAdded && dialog != null && dialog!!.isShowing) {
                dismiss()
            }
        }, 3000)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            // Ensure window is transparent
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel pending dismiss handler
        dismissHandler.removeCallbacksAndMessages(null)
        _binding = null
    }

    private fun loadMatchData() {
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
                    RenderEffect.createBlurEffect(30f, 30f, Shader.TileMode.CLAMP),
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

        // Play all animations sequentially
        AnimatorSet().apply {
            playSequentially(iconAnimator, titleAnimator, messageAlpha)
            start()
        }
    }

    companion object {
        private const val ARG_MATCHED_USER_ID = "matched_user_id"
        private const val ARG_MATCHED_USER_PHOTO = "matched_user_photo"

        fun newInstance(
            matchedUserId: String?,
            matchedUserPhotoUrl: String?,
        ) = MatchOverlayDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_MATCHED_USER_ID, matchedUserId)
                putString(ARG_MATCHED_USER_PHOTO, matchedUserPhotoUrl)
            }
        }
    }
}
