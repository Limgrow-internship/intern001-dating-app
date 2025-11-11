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

    private var maxSteps: Int = 5
    var currentStep: Int = 0
        set(value) {
            field = value.coerceIn(0, maxSteps)
            invalidate()
        }

    private var progressColor: Int = ContextCompat.getColor(context, R.color.bottom_nav_selected)
    private var backgroundColor: Int = ContextCompat.getColor(context, R.color.bottom_nav_unselected)

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = progressColor
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = backgroundColor
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StepProgressBar,
            0,
            0,
        ).apply {
            try {
                maxSteps = getInt(R.styleable.StepProgressBar_maxSteps, 5)
                currentStep = getInt(R.styleable.StepProgressBar_currentStep, 0)
                progressColor = getColor(
                    R.styleable.StepProgressBar_progressColor,
                    ContextCompat.getColor(context, R.color.bottom_nav_selected),
                )
                progressPaint.color = progressColor
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val progressWidth = viewWidth * currentStep / maxSteps

        // Draw background
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, backgroundPaint)

        // Draw progress
        if (currentStep > 0) {
            canvas.drawRect(0f, 0f, progressWidth, viewHeight, progressPaint)
        }
    }

    fun setMaxSteps(max: Int) {
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
