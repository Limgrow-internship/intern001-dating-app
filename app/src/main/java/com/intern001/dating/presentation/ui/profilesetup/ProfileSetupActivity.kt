package com.intern001.dating.presentation.ui.profilesetup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.databinding.ActivityProfileSetupBinding
import com.intern001.dating.presentation.common.state.UiState
import com.intern001.dating.presentation.common.ui.GenderSelector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding
    private val viewModel: ProfileSetupViewModel by viewModels()

    private var currentStep = 0

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.photoUrl = it.toString()
            Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        showStep(0)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.loadingProgress.visibility = View.VISIBLE
                        }
                        is UiState.Success -> {
                            binding.loadingProgress.visibility = View.GONE
                            Toast.makeText(this@ProfileSetupActivity, "Profile setup completed!", Toast.LENGTH_SHORT).show()
                            navigateToHome()
                        }
                        is UiState.Error -> {
                            binding.loadingProgress.visibility = View.GONE
                            Toast.makeText(this@ProfileSetupActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            binding.loadingProgress.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun showStep(step: Int) {
        currentStep = step

        binding.stepPhoto.root.visibility = View.GONE
        binding.stepName.root.visibility = View.GONE
        binding.stepGender.root.visibility = View.GONE
        binding.stepBirthday.root.visibility = View.GONE
        binding.stepGoal.root.visibility = View.GONE

        when (step) {
            0 -> setupPhotoStep()
            1 -> setupNameStep()
            2 -> setupGenderStep()
            3 -> setupBirthdayStep()
            4 -> setupGoalStep()
        }
    }

    private fun setupPhotoStep() {
        binding.stepPhoto.root.visibility = View.VISIBLE
        binding.stepPhoto.progressBar.currentStep = 0
        binding.stepPhoto.tvProgress.text = "0%"

        binding.stepPhoto.flPhoto1.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.stepPhoto.flPhoto2.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.stepPhoto.flPhoto3.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.stepPhoto.btnNext.setOnClickListener {
            showStep(1)
        }
    }

    private fun setupNameStep() {
        binding.stepName.root.visibility = View.VISIBLE
        binding.stepName.progressBar.currentStep = 1
        binding.stepName.tvProgress.text = "20%"

        binding.stepName.etName.setText(viewModel.name)

        binding.stepName.btnNext.setOnClickListener {
            val name = binding.stepName.etName.text.toString().trim()
            if (name.isEmpty()) {
                binding.stepName.tilName.error = "Please enter your name"
                return@setOnClickListener
            }
            viewModel.name = name
            showStep(2)
        }
    }

    private fun setupGenderStep() {
        binding.stepGender.root.visibility = View.VISIBLE
        binding.stepGender.progressBar.currentStep = 2
        binding.stepGender.tvProgress.text = "40%"

        binding.stepGender.genderSelector.setOnGenderSelectedListener { gender ->
            viewModel.gender = when (gender) {
                GenderSelector.Gender.MALE -> "male"
                GenderSelector.Gender.FEMALE -> "female"
                GenderSelector.Gender.OTHER -> "other"
                else -> ""
            }
        }

        binding.stepGender.btnNext.setOnClickListener {
            if (viewModel.gender.isEmpty()) {
                Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showStep(3)
        }
    }

    private fun setupBirthdayStep() {
        binding.stepBirthday.root.visibility = View.VISIBLE
        binding.stepBirthday.progressBar.currentStep = 3
        binding.stepBirthday.tvProgress.text = "60%"

        val calendar = Calendar.getInstance()
        calendar.set(2004, Calendar.APRIL, 1)

        binding.stepBirthday.llDatePicker.setOnClickListener {
            showDatePicker()
        }

        binding.stepBirthday.btnNext.setOnClickListener {
            if (viewModel.dateOfBirth.isEmpty()) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                viewModel.dateOfBirth = dateFormat.format(calendar.time)
            }
            showStep(4)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.set(2004, Calendar.APRIL, 1)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                viewModel.dateOfBirth = dateFormat.format(selectedCalendar.time)

                binding.stepBirthday.tvMonth.text = SimpleDateFormat("MMMM", Locale.getDefault()).format(selectedCalendar.time)
                binding.stepBirthday.tvDay.text = String.format("%02d", dayOfMonth)
                binding.stepBirthday.tvYear.text = year.toString()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun setupGoalStep() {
        binding.stepGoal.root.visibility = View.VISIBLE
        binding.stepGoal.progressBar.currentStep = 4
        binding.stepGoal.tvProgress.text = "80%"

        val goalButtons = listOf(
            binding.stepGoal.btnNewFriends to "New friends",
            binding.stepGoal.btnSomethingCasual to "Something casual",
            binding.stepGoal.btnJustVibing to "Just vibing",
            binding.stepGoal.btnOpenToAnything to "Open to anything",
            binding.stepGoal.btnStillFiguringOut to "Still figuring it out"
        )

        goalButtons.forEach { (button, goal) ->
            button.setOnClickListener {
                viewModel.goal = goal
                goalButtons.forEach { (btn, _) ->
                    btn.setBackgroundResource(R.drawable.bg_gender_unselected)
                    btn.setTextColor(getColor(R.color.black))
                }
                button.setBackgroundResource(R.drawable.bg_button_orange)
                button.setTextColor(getColor(R.color.white))
            }
        }

        binding.stepGoal.ivSkip.setOnClickListener {
            navigateToHome()
        }

        binding.stepGoal.btnNext.setOnClickListener {
            if (viewModel.goal.isEmpty()) {
                Toast.makeText(this, "Please select a goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.updateProfile()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentStep > 0) {
            showStep(currentStep - 1)
        } else {
            super.onBackPressed()
        }
    }
}
