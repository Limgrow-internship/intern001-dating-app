package com.intern001.dating.presentation.ui.forgot

import android.os.Bundle
import com.intern001.dating.R
import com.intern001.dating.databinding.ActivityForgotBinding
import com.intern001.dating.presentation.common.viewmodel.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun getNavHostFragmentId(): Int {
        return R.id.forgotContainer
    }
}
