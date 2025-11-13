package com.intern001.dating.presentation.ui.signup

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentInfoBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InfoFragment : BaseFragment() {

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    private var isPasswordVisible = false
    private val viewModel: InfoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        val view = binding.root

        setupPasswordVisibilityToggle()
        setupSignInButton()

        return view
    }

    private fun setupPasswordVisibilityToggle() {
        binding.etPassword.setOnTouchListener { _, event ->
            val drawableEnd = 2
            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = binding.etPassword.compoundDrawables[drawableEnd]
                if (drawable != null && event.rawX >= (binding.etPassword.right - drawable.bounds.width())) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun setupSignInButton() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passwordPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
            if (!passwordPattern.matches(password)) {
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            } else {
                binding.tvError.visibility = View.GONE
            }

            viewModel.sendOtp(email, password) { message ->
                // Hide loading animation
                showLoading(false)

                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                if (message.contains("OTP has been sent")) {
                    val verifyFragment = VerifyFragment().apply {
                        arguments = Bundle().apply {
                            putString("email", email)
                            putString("password", password)
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frg_container, verifyFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnSignIn.isEnabled = !isLoading
        binding.btnSignIn.text = if (isLoading) "" else getString(R.string.sign_up)
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        with(binding.etPassword) {
            if (isPasswordVisible) {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0)
            } else {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
            }
            setSelection(text.length)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
