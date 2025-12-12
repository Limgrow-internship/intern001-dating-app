package com.intern001.dating.presentation.ui.forgot

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentVerifyOtpBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyOtpFragment : BaseFragment() {

    private var _binding: FragmentVerifyOtpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by viewModels()

    private var email: String = ""
    private var resendTime = 60
    private val handler = Handler(Looper.getMainLooper())
    private var resendRunnable: Runnable? = null
    private var isVerifyingOtp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString("email").orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentVerifyOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvEmail.text = email
        setupOtpFields()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnVerify.setOnClickListener {
            val otp = getOtp()
            if (otp.length < 4) {
                Toast.makeText(requireContext(), "Please enter full OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            clearOtpError()
            isVerifyingOtp = true
            viewModel.verifyOtp(email, otp)
        }

        binding.tvResend.setOnClickListener {
            if (!binding.tvResend.isEnabled) return@setOnClickListener

            viewModel.requestOtp(email)
            resendTime = 60
            startResendCountdown()
        }

        startResendCountdown()
        observeState()
    }

    private fun setupOtpFields() {
        val otpFields = listOf(
            binding.otp1,
            binding.otp2,
            binding.otp3,
            binding.otp4,
        )

        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    when {
                        s?.length == 1 && index < otpFields.lastIndex ->
                            otpFields[index + 1].requestFocus()

                        s.isNullOrEmpty() && index > 0 ->
                            otpFields[index - 1].requestFocus()
                    }
                }
            })
        }
    }

    private fun getOtp(): String {
        return listOf(
            binding.otp1,
            binding.otp2,
            binding.otp3,
            binding.otp4,
        ).joinToString("") { it.text.toString() }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                ForgotPasswordViewModel.ForgotState.Loading -> {
                    if (isVerifyingOtp) {
                        binding.btnVerify.isEnabled = false
                    }
                }

                is ForgotPasswordViewModel.ForgotState.OtpVerified -> {
                    isVerifyingOtp = false
                    binding.btnVerify.isEnabled = true

                    findNavController().navigate(
                        R.id.action_verifyOtpFragment_to_resetPasswordFragment,
                        Bundle().apply {
                            putString("otpToken", state.otpToken)
                        },
                    )
                    viewModel.resetState()
                }

                is ForgotPasswordViewModel.ForgotState.Error -> {
                    if (isVerifyingOtp) {
                        binding.btnVerify.isEnabled = true
                        setOtpError()
                    }

                    isVerifyingOtp = false
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }

                else -> Unit
            }
        }
    }

    private fun setOtpError() {
        listOf(
            binding.otp1,
            binding.otp2,
            binding.otp3,
            binding.otp4,
        ).forEach {
            it.setBackgroundResource(R.drawable.bg_verify_otp_error)
        }
    }

    private fun clearOtpError() {
        listOf(
            binding.otp1,
            binding.otp2,
            binding.otp3,
            binding.otp4,
        ).forEach {
            it.setBackgroundResource(R.drawable.bg_verify_otp)
        }
    }

    private fun startResendCountdown() {
        binding.tvResend.isEnabled = false
        updateResendText()

        resendRunnable?.let { handler.removeCallbacks(it) }

        resendRunnable = object : Runnable {
            override fun run() {
                resendTime--
                if (resendTime <= 0) {
                    binding.tvResend.text =
                        getString(R.string.forgot_pass_verify_text3) // Resend
                    binding.tvResend.isEnabled = true
                } else {
                    updateResendText()
                    handler.postDelayed(this, 1000)
                }
            }
        }

        handler.postDelayed(resendRunnable!!, 1000)
    }

    private fun updateResendText() {
        binding.tvResend.text = "Resend (${resendTime}s)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resendRunnable?.let { handler.removeCallbacks(it) }
        _binding = null
    }
}
