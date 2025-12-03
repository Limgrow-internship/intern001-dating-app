package com.intern001.dating.presentation.ui.discover.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.cardview.widget.CardView
import androidx.viewpager2.widget.ViewPager2
import com.intern001.dating.R
import com.intern001.dating.databinding.ViewSwipeableCardBinding
import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.presentation.ui.discover.adapter.PhotoPagerAdapter

class SwipeableCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(context, attrs, defStyleAttr) {

    private val binding: ViewSwipeableCardBinding

    private var onSwipeListener: OnSwipeListener? = null
    private var onPhotoClickListener: OnPhotoClickListener? = null
    private var onActionClickListener: OnActionClickListener? = null

    private var initialX = 0f
    private var initialY = 0f
    private var dX = 0f
    private var dY = 0f

    private val swipeThreshold = 50f
    private val gestureDetector: GestureDetector
    private var parentDisallowIntercept = false
    private var isCardGesture = false
    private val photoSwipeEdgeRatio = 0.2f

    init {
        binding = ViewSwipeableCardBinding.inflate(LayoutInflater.from(context), this, true)

        val cornerRadius = 24f * context.resources.displayMetrics.density
        binding.viewPagerPhotos.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }
        binding.viewPagerPhotos.clipToOutline = true
        binding.viewPagerPhotos.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        gestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    handlePhotoTap(e.x)
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    onPhotoClickListener?.onLongPress()
                }
            },
        )

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

    private fun updateParentIntercept(disallow: Boolean) {
        if (parentDisallowIntercept == disallow) return
        parent?.requestDisallowInterceptTouchEvent(disallow)
        parentDisallowIntercept = disallow
    }

    private fun isTouchInPhotoSwipeZone(event: MotionEvent): Boolean {
        val photoView = binding.viewPagerPhotos
        val adapter = photoView.adapter

        if (adapter == null || adapter.itemCount <= 1) {
            return false
        }

        val withinVertical = event.y >= photoView.top && event.y <= photoView.bottom
        if (!withinVertical) return false

        val width = photoView.width.takeIf { it > 0 } ?: return false
        val relativeX = (event.x - photoView.left) / width.toFloat()
        val edge = photoSwipeEdgeRatio

        return relativeX in edge..(1f - edge)
    }

    fun bindCard(card: MatchCard) {
        val photoAdapter = PhotoPagerAdapter(card.photos)
        binding.viewPagerPhotos.adapter = photoAdapter
        setupPhotoIndicators(card.photos.size)
        binding.viewPagerPhotos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updatePhotoIndicators(position)
            }
        })

        binding.tvName.text = card.displayName

        val age = card.age
        binding.tvAge.text = if (age != null && age > 0) ", $age" else ""

        binding.tvGender.text = card.gender?.replaceFirstChar { it.uppercase() } ?: ""

        bindDistanceBadge(card.distance)
    }

    fun updateDistance(distanceKm: Double?) {
        bindDistanceBadge(distanceKm)
    }

    private fun setupPhotoIndicators(count: Int) {
        binding.photoIndicators.removeAllViews()
        if (count <= 1) {
            binding.photoIndicators.visibility = View.GONE
            return
        }

        binding.photoIndicators.visibility = View.VISIBLE
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
            val currentItem = binding.viewPagerPhotos.currentItem
            if (currentItem > 0) {
                binding.viewPagerPhotos.currentItem = currentItem - 1
            }
        } else {
            val currentItem = binding.viewPagerPhotos.currentItem
            val adapter = binding.viewPagerPhotos.adapter
            if (adapter != null && currentItem < adapter.itemCount - 1) {
                binding.viewPagerPhotos.currentItem = currentItem + 1
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                isCardGesture = !isTouchInPhotoSwipeZone(ev)
                initialX = x
                initialY = y
                dX = x - ev.rawX
                dY = y - ev.rawY
                updateParentIntercept(false)
                if (!isCardGesture) {
                    return false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isCardGesture) {
                    return false
                }
                val deltaX = kotlin.math.abs(ev.rawX + dX - initialX)
                val deltaY = kotlin.math.abs(ev.rawY + dY - initialY)
                if (deltaX > swipeThreshold && deltaX > deltaY) {
                    updateParentIntercept(true)
                    return true
                }
                if (deltaY > deltaX) {
                    updateParentIntercept(false)
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                isCardGesture = false
                updateParentIntercept(false)
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        if (!isCardGesture) {
            return super.onTouchEvent(event)
        }

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

                x = newX
                val rotation = deltaX / 20f
                this.rotation = rotation

                // Update swipe overlay based on direction
                updateSwipeOverlay(deltaX)
                return true
            }

            MotionEvent.ACTION_UP -> {
                val deltaX = x - initialX

                when {
                    deltaX > 200 -> {
                        animateSwipeOut(SwipeDirection.RIGHT)
                        onSwipeListener?.onLike()
                    }
                    deltaX < -200 -> {
                        animateSwipeOut(SwipeDirection.LEFT)
                        onSwipeListener?.onDislike()
                    }
                    else -> {
                        animateReturn()
                        resetSwipeOverlay()
                    }
                }
                updateParentIntercept(false)
                isCardGesture = false
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                animateReturn()
                resetSwipeOverlay()
                updateParentIntercept(false)
                isCardGesture = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateSwipeOverlay(deltaX: Float) {
        val swipeThreshold = 200f
        val alpha = (kotlin.math.abs(deltaX) / swipeThreshold).coerceIn(0f, 1f)

        if (deltaX > 0) {
            binding.likeOverlay.alpha = alpha
            binding.dislikeOverlay.alpha = 0f
        } else if (deltaX < 0) {
            binding.dislikeOverlay.alpha = alpha
            binding.likeOverlay.alpha = 0f
        } else {
            resetSwipeOverlay()
        }
    }

    private fun resetSwipeOverlay() {
        binding.likeOverlay.alpha = 0f
        binding.dislikeOverlay.alpha = 0f
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

    enum class SwipeDirection {
        LEFT,
        RIGHT,
        UP,
    }

    private fun bindDistanceBadge(distanceKm: Double?) {
        val (visible, text) = formatDistance(distanceKm)
        if (visible) {
            binding.locationBadge.visibility = View.VISIBLE
            binding.tvDistanceBadge.text = text
        } else {
            binding.locationBadge.visibility = View.GONE
        }
    }

    private fun formatDistance(distanceKm: Double?): Pair<Boolean, String> {
        if (distanceKm == null) {
            return false to ""
        }
        val meters = distanceKm * 1000
        return when {
            distanceKm < 0.0005 -> true to context.getString(R.string.distance_nearby)
            distanceKm < 1 -> {
                val meterText = context.getString(
                    R.string.distance_meter_format,
                    meters.toInt().coerceAtLeast(1),
                )
                true to meterText
            }
            else -> {
                val kmText = if (distanceKm < 10) {
                    context.getString(R.string.distance_km_one_decimal, distanceKm)
                } else {
                    context.getString(R.string.distance_km_format, distanceKm.toInt())
                }
                true to kmText
            }
        }
    }
}
