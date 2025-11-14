package com.intern001.dating.presentation.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentLoginBinding
import com.intern001.dating.presentation.common.state.UiState
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.signup.SignUpActivity
import com.intern001.dating.presentation.util.ValidationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val GOOGLE_SIGN_IN_REQUEST_CODE = 1001

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    viewModel.loginWithGoogle(idToken)
                } ?: run {
                    Snackbar.make(binding.root, "Google Sign-In failed: no token", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Snackbar.make(binding.root, "Google Sign-In failed: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }

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
            val intent = Intent(requireContext(), SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.btnForgotPass.setOnClickListener {
            Snackbar.make(binding.root, "Navigate to Forgot Password", Snackbar.LENGTH_SHORT).show()
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
                        binding.btnLogin.text = getString(R.string.login)
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
                        binding.btnLogin.text = getString(R.string.login)

                        navController.navigate(R.id.action_login_to_home)
                    }
                    is UiState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = getString(R.string.login)

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

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(requireContext(), SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
