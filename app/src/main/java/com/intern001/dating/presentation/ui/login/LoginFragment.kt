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
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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

        callbackManager = CallbackManager.Factory.create()

        setupGoogleSignIn()
        setupClickListeners()
        observeUiState()
        observeGoogleUiState()
        observeFacebookUiState()
    }

    private fun setupGoogleSignIn() {
        val webClientId = getString(R.string.default_web_client_id)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.btnGoogle.setOnClickListener { safeGoogleLogin() }

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(requireContext(), SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.btnForgotPass.setOnClickListener {
            Snackbar.make(binding.root, "Navigate to Forgot Password", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnFacebook.setOnClickListener {
            binding.progressBar.isVisible = true
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile", "pages_show_list", "pages_read_engagement"),
            )
            LoginManager.getInstance().registerCallback(
                callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        val fbToken = result.accessToken.token
                        viewModel.loginWithFacebook(fbToken)
                    }
                    override fun onCancel() {
                        binding.progressBar.isVisible = false
                        Snackbar.make(binding.root, "Facebook login cancelled", Snackbar.LENGTH_SHORT).show()
                    }
                    override fun onError(error: FacebookException) {
                        binding.progressBar.isVisible = false
                        Snackbar.make(binding.root, "Facebook login error: ${error.message}", Snackbar.LENGTH_LONG).show()
                    }
                },
            )
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

        callbackManager.onActivityResult(requestCode, resultCode, data)

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
                Log.d("GoogleLogin", "FULL_TOKEN = $idToken")

                FirebaseAuth.getInstance()
                    .signInWithCredential(GoogleAuthProvider.getCredential(account.idToken, null))
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            FirebaseAuth.getInstance().currentUser?.getIdToken(true)
                                ?.addOnCompleteListener { tokenTask ->
                                    val firebaseIdToken = tokenTask.result?.token
                                    if (firebaseIdToken != null) {
                                        viewModel.googleLogin(firebaseIdToken)
                                    } else {
                                        logError("Firebase idToken null")
                                    }
                                }
                        } else {
                            logError("Firebase signIn failed")
                        }
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
                        ).show()
                    }
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

    private fun observeFacebookUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fbUiState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                        binding.progressBar.isVisible = false
                        binding.btnFacebook.isEnabled = true
                    }
                    is UiState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.btnFacebook.isEnabled = false
                    }
                    is UiState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.btnFacebook.isEnabled = true
                        val displayName = state.data.profile.firstName?.let { firstName ->
                            state.data.profile.lastName?.let { lastName ->
                                "$firstName $lastName".trim()
                            } ?: firstName
                        } ?: state.data.profile.lastName ?: "Friend"
                        Snackbar.make(
                            binding.root,
                            "Hello $displayName",
                            Snackbar.LENGTH_LONG,
                        ).show()
                        navController.navigate(R.id.action_login_to_home)
                    }
                    is UiState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.btnFacebook.isEnabled = true
                        Snackbar.make(
                            binding.root,
                            state.message,
                            Snackbar.LENGTH_LONG,
                        ).show()
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
