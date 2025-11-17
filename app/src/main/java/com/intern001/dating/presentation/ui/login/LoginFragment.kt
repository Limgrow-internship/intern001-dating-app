package com.intern001.dating.presentation.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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

@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val GOOGLE_SIGN_IN_REQUEST_CODE = 1001

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var callbackManager: CallbackManager

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

        setupGoogleSignIn()
        setupClickListeners()
        observeUiState()
        observeGoogleUiState()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Bắt buộc Web client ID
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (validateInputs(email, password)) viewModel.login(email, password)
        }

        binding.btnGoogle.setOnClickListener { safeGoogleLogin() }

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(requireContext(), SignUpActivity::class.java))
        }

        binding.btnForgotPass.setOnClickListener {
            Snackbar.make(binding.root, "Navigate to Forgot Password", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun safeGoogleLogin() {
        googleSignInClient.signOut().addOnCompleteListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, GOOGLE_SIGN_IN_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            if (data == null) {
                logError("Google login failed: intent is null")
                return
            }

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken

                Log.d("GoogleLogin", "idToken=${idToken?.take(20)}... length=${idToken?.length}")
                Log.d("GoogleLogin", "account.email=${account?.email}, account.id=${account?.id}")

                if (!idToken.isNullOrEmpty()) {
                    viewModel.googleLogin(idToken)
                } else {
                    googleSignInClient.signOut()
                    logError("Google login failed: idToken null, force sign out")
                }
            } catch (e: ApiException) {
                logError("Google login ApiException code=${e.statusCode}, message=${e.message}")
                if (e.statusCode == 401) googleSignInClient.signOut()
            } catch (e: Exception) {
                logError("Google login unexpected error: ${e::class.java.simpleName}, message=${e.message}")
            }
        }
    }

    private fun logError(msg: String) {
        Log.e("GoogleLogin", msg)
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }

    private fun validateInputs(email: String, password: String): Boolean {
        val isEmailValid = ValidationHelper.validateEmail(email, binding.tilEmail)
        val isPasswordValid = ValidationHelper.validatePassword(password, binding.tilPassword)
        return isEmailValid && isPasswordValid
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Idle -> handleIdle()
                    is UiState.Loading -> handleLoading()
                    is UiState.Success -> handleSuccess()
                    is UiState.Error -> handleError(state.message)
                }
            }
        }
    }

    private fun observeGoogleUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.googleUiState.collect { state ->
                when (state) {
                    is UiState.Idle -> {}
                    is UiState.Loading -> handleLoading()
                    is UiState.Success -> handleSuccess()
                    is UiState.Error -> handleError(state.message)
                }
            }
        }
    }

    private fun handleIdle() {
        binding.progressBar.isVisible = false
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = getString(R.string.login)
    }

    private fun handleLoading() {
        binding.progressBar.isVisible = true
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = ""
        ValidationHelper.clearError(binding.tilEmail)
        ValidationHelper.clearError(binding.tilPassword)
    }

    private fun handleSuccess() {
        binding.progressBar.isVisible = false
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = getString(R.string.login)
        navController.navigate(R.id.action_login_to_home)
    }

    private fun handleError(message: String) {
        binding.progressBar.isVisible = false
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = getString(R.string.login)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
