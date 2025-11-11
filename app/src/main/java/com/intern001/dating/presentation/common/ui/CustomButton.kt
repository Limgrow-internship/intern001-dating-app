package com.intern001.dating.presentation.common.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.intern001.dating.R
import com.intern001.dating.databinding.ViewCustomButtonBinding

class CustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewCustomButtonBinding =
        ViewCustomButtonBinding.inflate(LayoutInflater.from(context), this, true)

    enum class ButtonStyle {
        PRIMARY, // Black background, white text
        SECONDARY, // Orange background, white text
        OUTLINE, // White background, black text with border
    }

    var text: String = ""
        set(value) {
            field = value
            binding.buttonText.text = value
        }

    var isEnabled: Boolean = true
        set(value) {
            field = value
            binding.root.isEnabled = value
            binding.root.alpha = if (value) 1f else 0.5f
        }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomButton,
            0,
            0,
        ).apply {
            try {
                text = getString(R.styleable.CustomButton_buttonText) ?: "Button"
                val styleIndex = getInt(R.styleable.CustomButton_buttonStyle, 0)
                setButtonStyle(ButtonStyle.values()[styleIndex])
            } finally {
                recycle()
            }
        }
    }

    fun setButtonStyle(style: ButtonStyle) {
        when (style) {
            ButtonStyle.PRIMARY -> {
                binding.root.setBackgroundResource(R.drawable.bg_button_black)
                binding.buttonText.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            ButtonStyle.SECONDARY -> {
                binding.root.setBackgroundResource(R.drawable.bg_button_orange)
                binding.buttonText.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            ButtonStyle.OUTLINE -> {
                binding.root.setBackgroundResource(R.drawable.bg_button_outline)
                binding.buttonText.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.root.setOnClickListener(l)
    }
}
