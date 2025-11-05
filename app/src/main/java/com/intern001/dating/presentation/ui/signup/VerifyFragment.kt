package com.intern001.dating.presentation.ui.signup

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.intern001.dating.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyFragment : BaseFragment() {

    interface OnVerificationSuccessListener {
        fun onVerificationSuccess()
    }

    private var listener: OnVerificationSuccessListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnVerificationSuccessListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_verify, container, false)

        val otpFields = listOf<EditText>(
            view.findViewById(R.id.otp1),
            view.findViewById(R.id.otp2),
            view.findViewById(R.id.otp3),
            view.findViewById(R.id.otp4),
            view.findViewById(R.id.otp5),
            view.findViewById(R.id.otp6),
        )

        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    } else if (s?.isEmpty() == true && i > 0) {
                        otpFields[i - 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        val btnVerify = view.findViewById<Button>(R.id.btnVerify)
        btnVerify.setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString() }

            if (otp.length < 6) {
                Toast.makeText(requireContext(), "Please enter full 6 OTP digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (otp == "123456") {
                listener?.onVerificationSuccess()
            } else {
                Toast.makeText(requireContext(), "OTP not valid", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
