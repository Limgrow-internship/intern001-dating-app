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
        PRIMARY,
        SECONDARY,
        OUTLINE,
    }

    var text: String = ""
        set(value) {
            field = value
            binding.buttonText.text = value
        }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.root.alpha = if (enabled) 1f else 0.5f
    }

    init {
        // Set default style
        setButtonStyle(ButtonStyle.PRIMARY)
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
