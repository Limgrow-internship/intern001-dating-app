package com.intern001.dating.presentation.ui.login

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentLoginBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var isPasswordVisible = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)

        etPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val drawable = etPassword.compoundDrawables[drawableEnd]
                if (drawable != null && event.rawX >= (etPassword.right - drawable.bounds.width())) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0)
                    } else {
                        etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
                    }
                    etPassword.setSelection(etPassword.text?.length ?: 0)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
