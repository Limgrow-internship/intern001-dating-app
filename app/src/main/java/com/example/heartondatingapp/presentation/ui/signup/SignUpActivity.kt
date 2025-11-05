package com.example.heartondatingapp.presentation.ui.signup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.heartondatingapp.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import com.example.heartondatingapp.R

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity(), VerifyFragment.OnVerificationSuccessListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Hiển thị InfoFragment đầu tiên
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

