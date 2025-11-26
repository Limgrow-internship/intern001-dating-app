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
import com.intern001.dating.databinding.ItemMatchCardExpandedBinding
import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.presentation.ui.discover.adapter.PhotoPagerAdapter

class MatchCardExpandedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(context, attrs, defStyleAttr) {

    private val binding: ItemMatchCardExpandedBinding

    private var onSwipeListener: OnSwipeListener? = null
    private var onPhotoClickListener: OnPhotoClickListener? = null
    private var onActionClickListener: OnActionClickListener? = null
    private var onOverlayTapListener: OnOverlayTapListener? = null

    private var initialX = 0f
    private var initialY = 0f
    private var dX = 0f
    private var dY = 0f

    private var isScrolling = false
    private val swipeThreshold = 50f
    private val gestureDetector: GestureDetector

    init {
        binding = ItemMatchCardExpandedBinding.inflate(LayoutInflater.from(context), this, true)

        gestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    // Handle photo tap navigation
                    handlePhotoTap(e.x)
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    onPhotoClickListener?.onLongPress()
                }
            },
        )
        // Setup action button listeners
        setupActionButtons()
    }

    private fun setupActionButtons() {
        binding.btnBack.setOnClickListener {
            onActionClickListener?.onBackClick()
        }
        binding.btnDislike.setOnClickListener {
            animateSwipeOut(SwipeDirection.LEFT)
            onSwipeListener?.onDislike()
        }
        binding.btnSuperLike.setOnClickListener {
            animateSwipeOut(SwipeDirection.UP)
            onSwipeListener?.onSuperLike()
        }
        binding.btnLike.setOnClickListener {
            animateSwipeOut(SwipeDirection.RIGHT)
            onSwipeListener?.onLike()
        }
    }

    private fun showDetailedInfo() {
        // Show all detailed info sections
        binding.detailInfoContainer.visibility = View.VISIBLE
    }

    fun hideDarkOverlayImmediate() {
        binding.darkOverlay.visibility = View.GONE
        binding.tvTapToMeet.visibility = View.GONE
        binding.darkOverlay.alpha = 0f
        showDetailedInfo()
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

        // Bind basic data
        binding.tvName.text = card.displayName

        // Calculate and display age
        val age = card.age ?: calculateAge(card)
        binding.tvAge.text = if (age != null && age > 0) ", $age" else ""

        // Gender
        binding.tvGender.text = card.gender?.replaceFirstChar { it.uppercase() } ?: ""

        // Distance badge on top-left
        val distance = card.distance?.toInt() ?: 0
        binding.tvDistanceBadge.text = context.getString(R.string.km_format, distance)

        // Bio
        if (!card.bio.isNullOrEmpty()) {
            binding.tvBio.text = card.bio
            binding.tvBio.visibility = View.VISIBLE
        } else {
            binding.tvBio.visibility = View.GONE
        }

        // Occupation
        if (!card.occupation.isNullOrEmpty()) {
            binding.tvOccupation.text = card.occupation
            binding.occupationSection.visibility = View.VISIBLE
        } else {
            binding.occupationSection.visibility = View.GONE
        }

        // Education
        if (!card.education.isNullOrEmpty()) {
            binding.tvEducation.text = card.education
            binding.educationSection.visibility = View.VISIBLE
        } else {
            binding.educationSection.visibility = View.GONE
        }

        // Location detail
        card.location?.let { loc ->
            if (!loc.city.isNullOrEmpty()) {
                val locationText = buildString {
                    append(loc.city)
                    if (!loc.country.isNullOrEmpty()) {
                        append(", ")
                        append(loc.country)
                    }
                }
                binding.tvLocationDetail.text = locationText
                binding.locationSection.visibility = View.VISIBLE
            } else {
                binding.locationSection.visibility = View.GONE
            }
        } ?: run {
            binding.locationSection.visibility = View.GONE
        }

        // Zodiac
        if (!card.zodiacSign.isNullOrEmpty()) {
            binding.tvZodiac.text = card.zodiacSign
            binding.zodiacSection.visibility = View.VISIBLE
        } else {
            binding.zodiacSection.visibility = View.GONE
        }

        // More button visibility
        val hasMoreInfo = !card.occupation.isNullOrEmpty() ||
            !card.education.isNullOrEmpty() ||
            card.location != null ||
            !card.zodiacSign.isNullOrEmpty()
        binding.tvMore.visibility = if (hasMoreInfo) View.VISIBLE else View.GONE

        // Looking for chips
        if (!card.relationshipMode.isNullOrEmpty()) {
            binding.tvLookingForTitle.visibility = View.VISIBLE
            binding.chipGroupLookingFor.visibility = View.VISIBLE
            binding.chipGroupLookingFor.removeAllViews()

            // Add relationship mode as chips
            val lookingForItems = listOf(card.relationshipMode)
            lookingForItems.forEach { item ->
                val chip = createChip(item)
                binding.chipGroupLookingFor.addView(chip)
            }
        } else {
            binding.tvLookingForTitle.visibility = View.GONE
            binding.chipGroupLookingFor.visibility = View.GONE
        }

        // Interests chips
        if (card.interests.isNotEmpty()) {
            binding.tvInterestsTitle.visibility = View.VISIBLE
            binding.chipGroupInterests.visibility = View.VISIBLE
            binding.chipGroupInterests.removeAllViews()

            card.interests.forEach { interest ->
                val chip = createChip(interest)
                binding.chipGroupInterests.addView(chip)
            }
        } else {
            binding.tvInterestsTitle.visibility = View.GONE
            binding.chipGroupInterests.visibility = View.GONE
        }
    }

    private fun calculateAge(card: MatchCard): Int? {
        // If there's a birthDate field, calculate from it
        // For now, return the age from the card
        return card.age
    }

    private fun createChip(text: String): Chip {
        return Chip(context).apply {
            this.text = text
            isClickable = false
            isCheckable = false
            setChipBackgroundColorResource(R.color.gray_100)
            setTextColor(context.getColor(R.color.black))
            chipStrokeWidth = 0f
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

    private fun isTouchInSwipeArea(ev: MotionEvent): Boolean {
        // Swipe area is from top to bottom of action buttons container
        val location = IntArray(2)
        binding.bottomInfoContainer.getLocationOnScreen(location)
        val bottomInfoBottom = location[1] + binding.bottomInfoContainer.height

        // Get touch Y position on screen
        return ev.rawY <= bottomInfoBottom
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Only intercept if touch is in swipe area (image + bottom info)
        if (!isTouchInSwipeArea(ev)) {
            return false
        }

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = x
                initialY = y
                dX = x - ev.rawX
                dY = y - ev.rawY
                isScrolling = false
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = kotlin.math.abs(ev.rawX + dX - initialX)
                val deltaY = kotlin.math.abs(ev.rawY + dY - initialY)
                // Intercept horizontal swipes, let vertical scroll through
                if (deltaX > swipeThreshold && deltaX > deltaY) {
                    return true
                }
            }
        }
        return false
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
                val deltaX = newX - initialX

                // Only move card horizontally for swipe
                x = newX
                val rotation = deltaX / 20f
                this.rotation = rotation
                return true
            }

            MotionEvent.ACTION_UP -> {
                val deltaX = x - initialX

                when {
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
    }

    private fun resetPosition() {
        x = initialX
        y = initialY
        rotation = 0f
    }

    fun setOnSwipeListener(listener: OnSwipeListener) {
        this.onSwipeListener = listener
    }

    fun setOnPhotoClickListener(listener: OnPhotoClickListener) {
        this.onPhotoClickListener = listener
    }

    fun setOnActionClickListener(listener: OnActionClickListener) {
        this.onActionClickListener = listener
    }

    fun setOnOverlayTapListener(listener: OnOverlayTapListener) {
        this.onOverlayTapListener = listener
    }

    interface OnSwipeListener {
        fun onLike()
        fun onDislike()
        fun onSuperLike()
    }

    interface OnPhotoClickListener {
        fun onLongPress()
    }

    interface OnActionClickListener {
        fun onBackClick()
    }

    interface OnOverlayTapListener {
        fun onOverlayTap()
    }

    enum class SwipeDirection {
        LEFT,
        RIGHT,
        UP,
    }
}
