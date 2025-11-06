package com.intern001.dating.presentation.ui.signup

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.intern001.dating.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InfoFragment : BaseFragment() {

    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info, container, false)
        val btnSignIn = view.findViewById<Button>(R.id.btnSignIn)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)

        etPassword.setOnTouchListener { _, event ->
            val drawableEnd = 2
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val drawable = etPassword.compoundDrawables[drawableEnd]
                if (drawable != null && event.rawX >= (etPassword.right - drawable.bounds.width())) {
                    togglePasswordVisibility(etPassword)
                    return@setOnTouchListener true
                }
            }
            false
        }

        btnSignIn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frg_container, VerifyFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun togglePasswordVisibility(editText: EditText) {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0)
        } else {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
        }
        editText.setSelection(editText.text.length)
    }
}
