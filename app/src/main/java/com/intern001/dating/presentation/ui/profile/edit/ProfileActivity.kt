package com.intern001.dating.presentation.ui.profile.edit

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.intern001.dating.R
import com.intern001.dating.databinding.ActivityProfileBinding
import com.intern001.dating.presentation.common.viewmodel.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    override fun getNavHostFragmentId(): Int = 0

    private lateinit var binding: ActivityProfileBinding

    private lateinit var editFragment: EditProfileFragment
    private lateinit var viewFragment: ViewProfileFragment
    private val viewModel: EditProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editFragment = EditProfileFragment()
        viewFragment = ViewProfileFragment()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, viewFragment)
            .hide(viewFragment)
            .commit()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, editFragment)
            .commit()

        setupTabs()
        setDefaultTab()
    }

    private fun setDefaultTab() {
        binding.motionNav.progress = 0f
        binding.btnEdit.setTextColor(Color.WHITE)
        binding.btnView.setTextColor(Color.GRAY)
    }

    private fun setupTabs() {
        binding.motionNav.setTransition(R.id.trans)

        binding.btnEdit.setOnClickListener {
            binding.motionNav.transitionToStart()
            binding.btnEdit.setTextColor(Color.WHITE)
            binding.btnView.setTextColor(Color.GRAY)
            showFragment(editFragment, viewFragment)
//            editFragment.loadUserProfile()
        }

        binding.btnView.setOnClickListener {
            binding.motionNav.transitionToEnd()
            binding.btnView.setTextColor(Color.WHITE)
            binding.btnEdit.setTextColor(Color.GRAY)
            showFragment(viewFragment, editFragment)
            viewFragment.refreshProfile()
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }


    private fun showFragment(show: Fragment, hide: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(hide)
            .show(show)
            .commit()
    }
}
