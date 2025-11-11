package com.intern001.dating.presentation.ui.signup

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import com.intern001.dating.R
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

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
    ): View? {
        val view = inflater.inflate(R.layout.fragment_verify, container, false)

        email = arguments?.getString("email")
        password = arguments?.getString("password")

        val tvEmailOtp = view.findViewById<TextView>(R.id.tvEmailOtp)
        val tvRecentCode = view.findViewById<TextView>(R.id.tvRecentCode)
        val btnVerify = view.findViewById<Button>(R.id.btnVerify)

        tvEmailOtp.text = email

        val otpFields = listOf(
            view.findViewById<EditText>(R.id.otp1),
            view.findViewById<EditText>(R.id.otp2),
            view.findViewById<EditText>(R.id.otp3),
            view.findViewById<EditText>(R.id.otp4),
        )

        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    } else if (s?.isEmpty() == true && i > 0) {
                        otpFields[i - 1].requestFocus()
                    }
                }
            })
        }

        btnVerify.setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString() }

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

        startCountdown(tvRecentCode)

        tvRecentCode.setOnClickListener {
            val emailValue = email
            val passwordValue = password

            if (emailValue.isNullOrEmpty() || passwordValue.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Missing email or password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tvRecentCode.isEnabled = false
            tvRecentCode.alpha = 0.5f
            tvRecentCode.text = "Resending..."

            viewModel.sendOtp(emailValue, passwordValue) { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                startCountdown(tvRecentCode)
            }
        }

        return view
    }

    private fun startCountdown(tvRecentCode: TextView) {
        countDownTimer?.cancel()

        tvRecentCode.isEnabled = false
        tvRecentCode.alpha = 0.5f

        countDownTimer = object : CountDownTimer(90000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvRecentCode.text = "Resend code in ${seconds}s"
            }

            override fun onFinish() {
                tvRecentCode.text = "Recent code"
                tvRecentCode.isEnabled = true
                tvRecentCode.alpha = 1f
            }
        }.start()
    }
}
