package com.intern001.dating.presentation.ui.signup

import android.content.Intent
import android.os.Bundle
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.presentation.common.viewmodel.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity :
    BaseActivity(),
    VerifyFragment.OnVerificationSuccessListener {
    override fun getNavHostFragmentId(): Int {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frg_container, InfoFragment())
            .commit()
    }

    override fun onVerificationSuccess() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
