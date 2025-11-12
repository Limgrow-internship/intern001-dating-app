package com.intern001.dating.presentation.ui.signup

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.intern001.dating.databinding.FragmentVerifyBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import com.intern001.dating.R

@AndroidEntryPoint
class VerifyFragment : BaseFragment() {

    interface OnVerificationSuccessListener {
        fun onVerificationSuccess()
    }

    private var listener: OnVerificationSuccessListener? = null
    private val viewModel: InfoViewModel by viewModels()

    private var email: String? = null
    private var password: String? = null
    private var countDownTimer: CountDownTimer? = null

    private var _binding: FragmentVerifyBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnVerificationSuccessListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        countDownTimer?.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentVerifyBinding.inflate(inflater, container, false)
        val view = binding.root

        email = arguments?.getString("email")
        password = arguments?.getString("password")

        setupOtpFields()
        setupVerifyButton()
        setupResendCode()

        binding.tvEmailOtp.text = email
        startCountdown()

        binding.btnBack.setOnClickListener {
            
        }

        return binding.root
    }

    private fun setupOtpFields() {
        val otpFields = listOf(
            binding.otp1,
            binding.otp2,
            binding.otp3,
            binding.otp4,
        )

        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    when {
                        s?.length == 1 && i < otpFields.size - 1 -> otpFields[i + 1].requestFocus()
                        s?.isEmpty() == true && i > 0 -> otpFields[i - 1].requestFocus()
                    }
                }
            })
        }
    }

    private fun setupVerifyButton() {
        binding.btnVerify.setOnClickListener {
            val otp = listOf(
                binding.otp1,
                binding.otp2,
                binding.otp3,
                binding.otp4,
            ).joinToString("") { it.text.toString() }

            if (otp.length < 4) {
                Toast.makeText(requireContext(), "Please enter full 4 OTP digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val emailValue = email ?: return@setOnClickListener
            viewModel.verifyOtp(emailValue, otp) { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                if (message.contains("success", ignoreCase = true) ||
                    message.contains("verified", ignoreCase = true)
                ) {
                    listener?.onVerificationSuccess()
                }
            }
        }
    }

    private fun setupResendCode() {
        binding.tvRecentCode.setOnClickListener {
            val emailValue = email
            val passwordValue = password

            if (emailValue.isNullOrEmpty() || passwordValue.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Missing email or password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.tvRecentCode.isEnabled = false
            binding.tvRecentCode.alpha = 0.5f
            binding.tvRecentCode.text = "Resending..."

            viewModel.sendOtp(emailValue, passwordValue) { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                startCountdown()
            }
        }
    }

    private fun startCountdown() {
        countDownTimer?.cancel()

        binding.tvRecentCode.isEnabled = false
        binding.tvRecentCode.alpha = 0.5f

        countDownTimer = object : CountDownTimer(90000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvRecentCode.text = "Resend code in ${seconds}s"
            }

            override fun onFinish() {
                binding.tvRecentCode.text = "Resend code"
                binding.tvRecentCode.isEnabled = true
                binding.tvRecentCode.alpha = 1f
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
