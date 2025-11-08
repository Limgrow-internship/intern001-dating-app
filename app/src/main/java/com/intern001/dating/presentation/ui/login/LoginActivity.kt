package com.intern001.dating.presentation.ui.login

import android.content.Intent
import android.os.Bundle
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.presentation.common.viewmodel.BaseActivity
import com.intern001.dating.presentation.ui.signup.VerifyFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity :
    BaseActivity(),
    VerifyFragment.OnVerificationSuccessListener {
    override fun getNavHostFragmentId(): Int {
        TODO("Not yet implemented")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportFragmentManager.beginTransaction()
            .replace(R.id.login_container, LoginFragment())
            .commit()
    }

    override fun onVerificationSuccess() {
        startActivity(
            Intent(this, MainActivity::class.java),
        )
        finish()
    }
}
