package com.intern001.dating.presentation.ui.profile

import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.intern001.dating.R
import com.intern001.dating.databinding.ActivityChangePasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val viewModel: ChangePasswordViewModel by viewModels()

    private lateinit var tvPasswordMismatch: TextView
    private lateinit var tvWeakPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvPasswordMismatch = binding.passwordFields.getChildAt(2) as TextView
        tvWeakPassword = binding.passwordFields.getChildAt(3) as TextView

        // Ẩn TextView lỗi lúc đầu
        tvPasswordMismatch.visibility = TextView.GONE
        tvWeakPassword.visibility = TextView.GONE

        // Setup toggle mắt
        setupPasswordToggle(binding.etNewPassword)
        setupPasswordToggle(binding.etConfirmNewPassword)

        binding.btnChangePassword.setOnClickListener {
            val newPass = binding.etNewPassword.text.toString()
            val confirmPass = binding.etConfirmNewPassword.text.toString()

            // Reset error
            tvPasswordMismatch.visibility = TextView.GONE
            tvWeakPassword.visibility = TextView.GONE

            if (newPass.isBlank() || confirmPass.isBlank()) {
                showToast("Please fill in all fields")
                return@setOnClickListener
            }

            // Kiểm tra mật khẩu mạnh
            if (!isPasswordStrong(newPass)) {
                tvWeakPassword.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            // Kiểm tra mật khẩu khớp
            if (newPass != confirmPass) {
                tvPasswordMismatch.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            viewModel.changePassword(newPass, confirmPass)
        }

        binding.btnBack.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        observeChangePasswordResult()
    }

    private fun isPasswordStrong(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
        return regex.matches(password)
    }

    private fun setupPasswordToggle(editText: EditText) {
        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (editText.right - drawableEnd.bounds.width())) {
                    togglePasswordVisibility(editText)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun togglePasswordVisibility(editText: EditText) {
        if (editText.transformationMethod is PasswordTransformationMethod) {
            editText.transformationMethod = null
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
        }
        editText.setSelection(editText.text.length)
    }

    private fun observeChangePasswordResult() {
        lifecycleScope.launchWhenStarted {
            viewModel.changePasswordState.collect { result ->
                result?.onSuccess { response ->
                    if (response.isSuccessful) {
                        showToast("Password changed successfully!")
                        lifecycleScope.launch {
                            delay(2000)
                            setResult(RESULT_OK)
                            finish()
                        }
                    } else {
                        showToast("Failed: ${response.code()} - ${response.message()}")
                    }
                }?.onFailure { e ->
                    showToast("Connection error: ${e.message}")
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
