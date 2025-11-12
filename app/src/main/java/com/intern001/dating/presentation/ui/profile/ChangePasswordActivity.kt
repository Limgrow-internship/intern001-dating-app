package com.intern001.dating.presentation.ui.profile

import android.os.Bundle
import android.text.InputType
import androidx.lifecycle.lifecycleScope
import android.view.MotionEvent
import android.widget.Toast
import com.intern001.dating.R
import androidx.activity.viewModels
import com.intern001.dating.databinding.ActivityChangePasswordBinding
import com.intern001.dating.presentation.common.viewmodel.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val viewModel: ChangePasswordViewModel by viewModels()

    override fun getNavHostFragmentId(): Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPasswordToggle(binding.etOldPassword)
        setupPasswordToggle(binding.etNewPassword)
        setupPasswordToggle(binding.etConfirmNewPassword)

        binding.btnChangePassword.setOnClickListener {
            val oldPass = binding.etOldPassword.text.toString()
            val newPass = binding.etNewPassword.text.toString()
            val confirm = binding.etConfirmNewPassword.text.toString()

            if (oldPass.isBlank() || newPass.isBlank() || confirm.isBlank()) {
                showToast("Please fill in all fields")
                return@setOnClickListener
            }

            viewModel.changePassword(oldPass, newPass, confirm)
        }

        binding.btnBack.setOnClickListener{
            finish()
        }

        observeChangePasswordResult()
    }

    private fun observeChangePasswordResult() {
        lifecycleScope.launchWhenStarted {
            viewModel.changePasswordState.collect { result ->
                result?.onSuccess { response ->
                    if (response.isSuccessful) {
                        showToast("Password changed successfully!")
                        kotlinx.coroutines.delay(5000)
                        finish()
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

    private fun setupPasswordToggle(editText: android.widget.EditText) {
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

    private fun togglePasswordVisibility(editText: android.widget.EditText) {
        val isVisible = editText.inputType ==
            (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0)
        }

        editText.setSelection(editText.text.length)
    }
}
