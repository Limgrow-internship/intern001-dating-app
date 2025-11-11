package com.intern001.dating.presentation.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentLoginBinding
import com.intern001.dating.presentation.common.state.UiState
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.util.ValidationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomNavigation(true)

        setupClickListeners()
        observeUiState()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.btnSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_profile_setup)
        }

        binding.btnForgotPass.setOnClickListener {
            Snackbar.make(binding.root, "Navigate to Forgot Password", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnGoogle.setOnClickListener {
            Snackbar.make(binding.root, "Google login coming soon", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnFacebook.setOnClickListener {
            Snackbar.make(binding.root, "Facebook login coming soon", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(
        email: String,
        password: String,
    ): Boolean {
        val isEmailValid = ValidationHelper.validateEmail(email, binding.tilEmail)
        val isPasswordValid = ValidationHelper.validatePassword(password, binding.tilPassword)

        return isEmailValid && isPasswordValid
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                        binding.progressBar.isVisible = false
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = getString(com.intern001.dating.R.string.login)
                    }
                    is UiState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.btnLogin.isEnabled = false
                        binding.btnLogin.text = ""
                        ValidationHelper.clearError(binding.tilEmail)
                        ValidationHelper.clearError(binding.tilPassword)
                    }
                    is UiState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = getString(com.intern001.dating.R.string.login)

                        Snackbar.make(
                            binding.root,
                            "Login successful!",
                            Snackbar.LENGTH_SHORT,
                        )
                            .show()

                        navController.navigate(com.intern001.dating.R.id.action_login_to_home)
                    }
                    is UiState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = getString(com.intern001.dating.R.string.login)

                        Snackbar.make(
                            binding.root,
                            state.message,
                            Snackbar.LENGTH_LONG,
                        )
                            .show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
