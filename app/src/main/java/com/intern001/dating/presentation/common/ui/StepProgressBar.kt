package com.intern001.dating.presentation.common.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.intern001.dating.R

class StepProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var maxSteps: Int = 10
    var currentStep: Int = 0
        set(value) {
            field = value.coerceIn(0, maxSteps)
            invalidate()
        }

    private var progressColor: Int = ContextCompat.getColor(context, R.color.bottom_nav_selected)
    private var backgroundColor: Int = ContextCompat.getColor(context, R.color.progress_color_loading)

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = progressColor
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = backgroundColor
    }

    init {
        // Default values set above
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val cornerRadius = 16f

        canvas.drawRoundRect(0f, 0f, viewWidth, viewHeight, cornerRadius, cornerRadius, backgroundPaint)

        if (currentStep > 0 && maxSteps > 0) {
            val progressWidth = viewWidth * currentStep / maxSteps
            canvas.drawRoundRect(0f, 0f, progressWidth, viewHeight, cornerRadius, cornerRadius, progressPaint)
        }
    }

    fun setMaxSteps(max: Int) {
        require(max > 0) { "maxSteps must be greater than 0, got: $max" }
        maxSteps = max
        currentStep = currentStep.coerceIn(0, maxSteps)
        invalidate()
    }

    fun nextStep() {
        if (currentStep < maxSteps) {
            currentStep++
        }
    }

    fun previousStep() {
        if (currentStep > 0) {
            currentStep--
        }
    }
}
