package com.intern001.dating.presentation.ui.forgot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentResetPasswordBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordFragment : BaseFragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by viewModels()

    private lateinit var otpToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        otpToken = arguments?.getString("otpToken")
            ?: throw IllegalStateException("otpToken is required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClick()
        observeState()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupClick() {
        binding.btnReset.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmNewPassword.text.toString().trim()

            when {
                newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(
                        requireContext(),
                        "Please fill all fields",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                newPassword.length < 8 -> {
                    Toast.makeText(
                        requireContext(),
                        "Password must be at least 8 characters",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                else -> {
                    viewModel.resetPassword(
                        otpToken = otpToken,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword,
                    )
                }
            }
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                ForgotPasswordViewModel.ForgotState.Loading -> {
                    binding.btnReset.isEnabled = false
                }

                ForgotPasswordViewModel.ForgotState.Success -> {
                    binding.btnReset.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        "Password reset successfully",
                        Toast.LENGTH_SHORT,
                    ).show()

                    findNavController().navigate(
                        R.id.action_resetPasswordFragment_to_resetPasswordSuccessFragment,
                    )

                    viewModel.resetState()
                }

                is ForgotPasswordViewModel.ForgotState.Error -> {
                    binding.btnReset.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_SHORT,
                    ).show()

                    viewModel.resetState()
                }

                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.resetState()
    }
}
