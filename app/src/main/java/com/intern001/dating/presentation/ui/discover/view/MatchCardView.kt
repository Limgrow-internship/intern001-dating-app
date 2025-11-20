package com.intern001.dating.presentation.ui.discover.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.chip.Chip
import com.intern001.dating.R
import com.intern001.dating.databinding.ItemMatchCardBinding
import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.presentation.ui.discover.adapter.PhotoPagerAdapter
import kotlin.math.abs

class MatchCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(context, attrs, defStyleAttr) {

    private val binding: ItemMatchCardBinding

    private var onSwipeListener: OnSwipeListener? = null
    private var onPhotoClickListener: OnPhotoClickListener? = null

    private var initialX = 0f
    private var initialY = 0f
    private var dX = 0f
    private var dY = 0f

    private val gestureDetector: GestureDetector

    init {
        binding = ItemMatchCardBinding.inflate(LayoutInflater.from(context), this, true)

        gestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    // Check if dark overlay is visible
                    if (binding.darkOverlay.isVisible) {
                        // Hide overlay when tapped
                        hideDarkOverlay()
                        return true
                    }
                    // Handle photo tap navigation
                    handlePhotoTap(e.x)
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    onPhotoClickListener?.onLongPress()
                }
            },
        )

        // Setup overlay click listener
        binding.darkOverlay.setOnClickListener {
            hideDarkOverlay()
        }

        // Show overlay by default
        showDarkOverlay()
    }

    private fun showDarkOverlay() {
        binding.darkOverlay.visibility = View.VISIBLE
        binding.tvTapToMeet.visibility = View.VISIBLE
    }

    private fun hideDarkOverlay() {
        binding.darkOverlay.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                binding.darkOverlay.visibility = View.GONE
                binding.tvTapToMeet.visibility = View.GONE
            }
            .start()
    }

    fun bindCard(card: MatchCard) {
        // Setup photo viewpager
        val photoAdapter = PhotoPagerAdapter(card.photos)
        binding.viewPagerPhotos.adapter = photoAdapter
        setupPhotoIndicators(card.photos.size)

        // Setup viewpager page change listener
        binding.viewPagerPhotos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updatePhotoIndicators(position)
            }
        })

        // Bind data
        binding.tvName.text = card.firstName
        binding.tvAge.text = card.age.toString()
        binding.tvOccupation.text = card.occupation ?: ""
        binding.tvOccupation.isVisible = !card.occupation.isNullOrEmpty()
        binding.tvBio.text = card.bio ?: ""
        binding.tvBio.isVisible = !card.bio.isNullOrEmpty()

        val distance = card.distance?.toInt() ?: 0
        binding.tvDistance.text = context.getString(R.string.km_away, distance.toString())

        binding.ivVerifiedBadge.isVisible = card.isVerified

        // Setup interests chips
        binding.chipGroupInterests.removeAllViews()
        card.interests?.take(3)?.forEach { interest ->
            val chip = Chip(context).apply {
                text = interest
                isClickable = false
                isCheckable = false
            }
            binding.chipGroupInterests.addView(chip)
        }
    }

    private fun setupPhotoIndicators(count: Int) {
        binding.photoIndicators.removeAllViews()
        if (count <= 1) {
            binding.photoIndicators.isVisible = false
            return
        }

        binding.photoIndicators.isVisible = true
        for (i in 0 until count) {
            val indicator = View(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    0,
                    context.resources.getDimensionPixelSize(R.dimen.indicator_height),
                    1f,
                ).apply {
                    if (i > 0) marginStart = context.resources.getDimensionPixelSize(R.dimen.indicator_margin)
                }
                setBackgroundResource(
                    if (i == 0) {
                        R.drawable.photo_indicator_active
                    } else {
                        R.drawable.photo_indicator_inactive
                    },
                )
            }
            binding.photoIndicators.addView(indicator)
        }
    }

    private fun updatePhotoIndicators(position: Int) {
        for (i in 0 until binding.photoIndicators.childCount) {
            val indicator = binding.photoIndicators.getChildAt(i)
            indicator.setBackgroundResource(
                if (i == position) {
                    R.drawable.photo_indicator_active
                } else {
                    R.drawable.photo_indicator_inactive
                },
            )
        }
    }

    private fun handlePhotoTap(x: Float) {
        val width = binding.viewPagerPhotos.width
        if (x < width / 2) {
            // Tap on left side - previous photo
            val currentItem = binding.viewPagerPhotos.currentItem
            if (currentItem > 0) {
                binding.viewPagerPhotos.currentItem = currentItem - 1
            }
        } else {
            // Tap on right side - next photo
            val currentItem = binding.viewPagerPhotos.currentItem
            val adapter = binding.viewPagerPhotos.adapter
            if (adapter != null && currentItem < adapter.itemCount - 1) {
                binding.viewPagerPhotos.currentItem = currentItem + 1
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = x
                initialY = y
                dX = x - event.rawX
                dY = y - event.rawY
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val newX = event.rawX + dX
                val newY = event.rawY + dY

                x = newX
                y = newY

                // Calculate swipe direction and update indicators
                val deltaX = newX - initialX
                val deltaY = newY - initialY
                val rotation = deltaX / 20f

                this.rotation = rotation

                // Update swipe indicators
                when {
                    deltaY < -200 -> {
                        // Super like (swipe up)
                        val alpha = (abs(deltaY) - 200f) / 200f
                        binding.swipeIndicatorSuperLike.alpha = alpha.coerceIn(0f, 1f)
                        binding.swipeIndicatorLike.alpha = 0f
                        binding.swipeIndicatorDislike.alpha = 0f
                    }
                    deltaX > 100 -> {
                        // Like (swipe right)
                        val alpha = (deltaX - 100f) / 200f
                        binding.swipeIndicatorLike.alpha = alpha.coerceIn(0f, 1f)
                        binding.swipeIndicatorDislike.alpha = 0f
                        binding.swipeIndicatorSuperLike.alpha = 0f
                    }
                    deltaX < -100 -> {
                        // Dislike (swipe left)
                        val alpha = (abs(deltaX) - 100f) / 200f
                        binding.swipeIndicatorDislike.alpha = alpha.coerceIn(0f, 1f)
                        binding.swipeIndicatorLike.alpha = 0f
                        binding.swipeIndicatorSuperLike.alpha = 0f
                    }
                    else -> {
                        // Reset indicators
                        binding.swipeIndicatorLike.alpha = 0f
                        binding.swipeIndicatorDislike.alpha = 0f
                        binding.swipeIndicatorSuperLike.alpha = 0f
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                val deltaX = x - initialX
                val deltaY = y - initialY

                when {
                    deltaY < -300 -> {
                        // Super like
                        animateSwipeOut(SwipeDirection.UP)
                        onSwipeListener?.onSuperLike()
                    }
                    deltaX > 200 -> {
                        // Like
                        animateSwipeOut(SwipeDirection.RIGHT)
                        onSwipeListener?.onLike()
                    }
                    deltaX < -200 -> {
                        // Dislike
                        animateSwipeOut(SwipeDirection.LEFT)
                        onSwipeListener?.onDislike()
                    }
                    else -> {
                        // Return to original position
                        animateReturn()
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun animateSwipeOut(direction: SwipeDirection) {
        val targetX = when (direction) {
            SwipeDirection.LEFT -> -width * 2f
            SwipeDirection.RIGHT -> width * 2f
            SwipeDirection.UP -> x
        }
        val targetY = when (direction) {
            SwipeDirection.UP -> -height * 2f
            else -> y
        }

        animate()
            .x(targetX)
            .y(targetY)
            .setDuration(200)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                    resetPosition()
                }
            })
            .start()
    }

    private fun animateReturn() {
        animate()
            .x(initialX)
            .y(initialY)
            .rotation(0f)
            .setDuration(200)
            .start()

        // Reset indicators
        binding.swipeIndicatorLike.animate().alpha(0f).setDuration(200).start()
        binding.swipeIndicatorDislike.animate().alpha(0f).setDuration(200).start()
        binding.swipeIndicatorSuperLike.animate().alpha(0f).setDuration(200).start()
    }

    private fun resetPosition() {
        x = initialX
        y = initialY
        rotation = 0f
        binding.swipeIndicatorLike.alpha = 0f
        binding.swipeIndicatorDislike.alpha = 0f
        binding.swipeIndicatorSuperLike.alpha = 0f
    }

    fun setOnSwipeListener(listener: OnSwipeListener) {
        this.onSwipeListener = listener
    }

    fun setOnPhotoClickListener(listener: OnPhotoClickListener) {
        this.onPhotoClickListener = listener
    }

    interface OnSwipeListener {
        fun onLike()
        fun onDislike()
        fun onSuperLike()
    }

    interface OnPhotoClickListener {
        fun onLongPress()
    }

    enum class SwipeDirection {
        LEFT,
        RIGHT,
        UP,
    }
}
