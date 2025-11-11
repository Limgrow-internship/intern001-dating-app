package com.intern001.dating.presentation.ui.signup

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.intern001.dating.R
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InfoFragment : BaseFragment() {

    private var isPasswordVisible = false
    private val viewModel: InfoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info, container, false)

        val btnSignIn = view.findViewById<Button>(R.id.btnSignIn)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)

        etPassword.setOnTouchListener { _, event ->
            val drawableEnd = 2
            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = etPassword.compoundDrawables[drawableEnd]
                if (drawable != null && event.rawX >= (etPassword.right - drawable.bounds.width())) {
                    togglePasswordVisibility(etPassword)
                    return@setOnTouchListener true
                }
            }
            false
        }

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "OTP sent successfully", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.sendOtp(email, password) { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                if (message.contains("OTP has been sent")) {
                    val verifyFragment = VerifyFragment()
                    val bundle = Bundle()
                    bundle.putString("email", email)
                    bundle.putString("password", password)
                    verifyFragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frg_container, verifyFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        return view
    }

    private fun togglePasswordVisibility(editText: EditText) {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
        }
        editText.setSelection(editText.text.length)
    }
}
