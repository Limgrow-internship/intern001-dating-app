package com.intern001.dating.presentation.ui.profile

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.databinding.ActivityChangePasswordBinding
import com.intern001.dating.presentation.common.viewmodel.BaseActivity
import com.intern001.dating.presentation.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val viewModel: ChangePasswordViewModel by viewModels()
    private lateinit var tokenManager: TokenManager

    override fun getNavHostFragmentId(): Int = 0

    private lateinit var tvPasswordMismatch: TextView
    private lateinit var tvWeakPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        tvPasswordMismatch = binding.passwordFields.getChildAt(2) as TextView
        tvWeakPassword = binding.passwordFields.getChildAt(3) as TextView

        tvPasswordMismatch.visibility = TextView.GONE
        tvWeakPassword.visibility = TextView.GONE

        setupPasswordToggle(binding.etNewPassword)
        setupPasswordToggle(binding.etConfirmNewPassword)

        binding.btnChangePassword.setOnClickListener {
            val newPass = binding.etNewPassword.text.toString()
            val confirmPass = binding.etConfirmNewPassword.text.toString()

            tvPasswordMismatch.visibility = TextView.GONE
            tvWeakPassword.visibility = TextView.GONE

            if (newPass.isBlank() || confirmPass.isBlank()) {
                showToast("Please fill in all fields")
                return@setOnClickListener
            }

            if (!isPasswordStrong(newPass)) {
                tvWeakPassword.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

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
                        showSuccessDialog()
                    } else {
                        showToast("Failed: ${response.code()} - ${response.message()}")
                    }
                }?.onFailure { e ->
                    showToast("Connection error: ${e.message}")
                }
            }
        }
    }

    private fun showSuccessDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_change_password_successfully)
        dialog.setCancelable(false)

        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setGravity(Gravity.BOTTOM)
        window?.attributes?.windowAnimations = R.style.DialogSlideAnimation
        window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val btnDone = dialog.findViewById<TextView>(R.id.btnDone)
        btnDone.setOnClickListener {
            lifecycleScope.launch {
                tokenManager.clearTokens()
                dialog.dismiss()
                val intent = Intent(this@ChangePasswordActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        dialog.show()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
