package com.intern001.dating.presentation.ui.forgot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentCheckEmailBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CheckEmailFragment : BaseFragment() {

    private var _binding: FragmentCheckEmailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCheckEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeState()

        binding.btnNext.setOnClickListener {
            validateAndNext()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun validateAndNext() {
        val email = binding.etEmail.text.toString().trim()

        if (!isValidEmail(email)) {
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = getString(R.string.forgot_pass_error)
            return
        }

        binding.tvError.visibility = View.GONE
        viewModel.requestOtp(email)
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ForgotPasswordViewModel.ForgotState.Loading -> {
                    showLoading(true)
                }

                is ForgotPasswordViewModel.ForgotState.OtpSent -> {
                    showLoading(false)
                    findNavController().navigate(
                        R.id.action_checkEmailFragment_to_verifyOtpFragment,
                        bundleOf("email" to binding.etEmail.text.toString().trim()),
                    )

                    viewModel.resetState()
                }

                is ForgotPasswordViewModel.ForgotState.Error -> {
                    showLoading(false)
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                }

                else -> Unit
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnNext.isEnabled = !isLoading
        binding.progressBarBottom.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnNext.text = if (isLoading) "" else getString(R.string.forgot_pass_btn)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
