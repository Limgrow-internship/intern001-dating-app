package com.intern001.dating.presentation.ui.login


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.heartondatingapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hiển thị LoginFragment lần đầu
        supportFragmentManager.beginTransaction()
            .replace(R.id.loginContainer, LoginFragment())
            .commit()
    }
}
