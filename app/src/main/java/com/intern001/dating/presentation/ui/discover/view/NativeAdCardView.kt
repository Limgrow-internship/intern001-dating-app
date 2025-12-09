package com.intern001.dating.presentation.ui.discover.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.cardview.widget.CardView
import com.google.android.gms.ads.nativead.NativeAd
import com.intern001.dating.databinding.ViewDiscoverNativeAdBinding
import com.intern001.dating.presentation.common.ads.NativeAdHelper

class NativeAdCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(context, attrs, defStyleAttr) {

    private val binding: ViewDiscoverNativeAdBinding =
        ViewDiscoverNativeAdBinding.inflate(LayoutInflater.from(context), this, true)

    private var onAdSwipeListener: OnAdSwipeListener? = null

    private var initialX = 0f
    private var initialY = 0f
    private var dX = 0f
    private var dY = 0f
    private val swipeThreshold = 200f
    private var isAnimatingSwipeOut = false

    fun bindNativeAd(nativeAd: NativeAd?) {
        NativeAdHelper.bindNativeAdFull(
            context,
            binding.adContainer,
            nativeAd,
        )
    }

    fun setOnAdSwipeListener(listener: OnAdSwipeListener?) {
        onAdSwipeListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isAnimatingSwipeOut) return true

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

                val deltaX = newX - initialX
                rotation = deltaX / 20f

                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }

            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                val deltaX = x - initialX
                val deltaY = y - initialY

                when {
                    deltaY < -300 -> animateSwipeOut(SwipeDirection.UP)
                    deltaX > swipeThreshold -> animateSwipeOut(SwipeDirection.RIGHT)
                    deltaX < -swipeThreshold -> animateSwipeOut(SwipeDirection.LEFT)
                    else -> animateReturn()
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                animateReturn()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun animateSwipeOut(direction: SwipeDirection) {
        isAnimatingSwipeOut = true
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
            .withEndAction {
                resetPosition()
                isAnimatingSwipeOut = false
                onAdSwipeListener?.onDismissed()
            }
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

    interface OnAdSwipeListener {
        fun onDismissed()
    }

    enum class SwipeDirection {
        LEFT,
        RIGHT,
        UP,
    }
}

