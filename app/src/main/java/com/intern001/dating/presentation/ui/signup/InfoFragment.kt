package com.intern001.dating.presentation.ui.signup

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentInfoBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.login.LoginActivity
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
        setupValidation()
        setupSignInButton()

        binding.tvLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

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

    private fun setupValidation() {
        val passwordPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")

        val etEmail = binding.etEmail
        val etPassword = binding.etPassword
        val tvPassHintLength = binding.tvPasswordHint
        val tvPassHintChar = binding.tvPasswordHint1
        val btnSignUp = binding.btnSignIn

        fun updateButtonState() {
            val emailValid = Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches()
            val passwordValid = passwordPattern.matches(etPassword.text.toString())
            btnSignUp.isEnabled = emailValid && passwordValid
        }

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString()
                if (email.isEmpty()) {
                    etEmail.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edittext)
                    etEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                } else if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edittext_correct)
                    etEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_password, 0)
                } else {
                    etEmail.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edittext_error)
                    etEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
                updateButtonState()
            }
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()

                if (password.length >= 8) {
                    tvPassHintLength.setTextColor(Color.GREEN)
                } else {
                    tvPassHintLength.setTextColor(Color.GRAY)
                }

                val hasLetter = password.any { it.isLetter() }
                val hasDigit = password.any { it.isDigit() }
                if (hasLetter && hasDigit) {
                    tvPassHintChar.setTextColor(Color.GREEN)
                } else {
                    tvPassHintChar.setTextColor(Color.GRAY)
                }

                if (passwordPattern.matches(password)) {
                    etPassword.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edittext_correct)
                    etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_password, 0)
                } else {
                    etPassword.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edittext)
                    etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }

                updateButtonState()
            }
        })
    }

    private fun setupSignInButton() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passwordPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")
            if (!passwordPattern.matches(password)) {
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            } else {
                binding.tvError.visibility = View.GONE
            }

            binding.progressBarBottom.visibility = View.VISIBLE
            binding.btnSignIn.text = ""
            binding.btnSignIn.isEnabled = false

            viewModel.sendOtp(email, password) { message ->
                binding.progressBarBottom.visibility = View.GONE
                binding.btnSignIn.text = getString(R.string.sign_up)
                binding.btnSignIn.isEnabled = true

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
