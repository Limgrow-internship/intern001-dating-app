package com.intern001.dating.presentation.ui.profilesetup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.intern001.dating.MainActivity
import com.intern001.dating.databinding.ActivityProfileSetupBinding
import com.intern001.dating.presentation.common.state.UiState
import com.intern001.dating.presentation.common.ui.GenderSelector
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding
    private val viewModel: ProfileSetupViewModel by viewModels()

    private var currentStep = 0

    private val pickImage1Launcher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            viewModel.photo1Url = it.toString()
            binding.stepPhoto.ivPhoto1.setImageURI(it)
            binding.stepPhoto.ivAdd1.visibility = View.GONE
            Toast.makeText(this, "Photo 1 selected", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImage2Launcher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            viewModel.photo2Url = it.toString()
            binding.stepPhoto.ivPhoto2.setImageURI(it)
            binding.stepPhoto.ivAdd2.visibility = View.GONE
            Toast.makeText(this, "Photo 2 selected", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImage3Launcher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            viewModel.photo3Url = it.toString()
            binding.stepPhoto.ivPhoto3.setImageURI(it)
            binding.stepPhoto.ivAdd3.visibility = View.GONE
            Toast.makeText(this, "Photo 3 selected", Toast.LENGTH_SHORT).show()
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

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showStep(step: Int) {
        currentStep = step

        binding.stepPhoto.root.visibility = View.GONE
        binding.stepName.root.visibility = View.GONE
        binding.stepGender.root.visibility = View.GONE
        binding.stepBirthday.root.visibility = View.GONE
        binding.stepMode.root.visibility = View.GONE

        when (step) {
            0 -> setupPhotoStep()
            1 -> setupNameStep()
            2 -> setupGenderStep()
            3 -> setupBirthdayStep()
            4 -> setupModeStep()
        }
    }

    private fun setupPhotoStep() {
        binding.stepPhoto.root.visibility = View.VISIBLE
        binding.stepPhoto.progressBar.currentStep = 0
        binding.stepPhoto.tvProgress.text = "0%"

        viewModel.photo1Url?.let {
            binding.stepPhoto.ivPhoto1.setImageURI(it.toUri())
            binding.stepPhoto.ivAdd1.visibility = View.GONE
        } ?: run {
            binding.stepPhoto.ivAdd1.visibility = View.VISIBLE
        }

        viewModel.photo2Url?.let {
            binding.stepPhoto.ivPhoto2.setImageURI(it.toUri())
            binding.stepPhoto.ivAdd2.visibility = View.GONE
        } ?: run {
            binding.stepPhoto.ivAdd2.visibility = View.VISIBLE
        }

        viewModel.photo3Url?.let {
            binding.stepPhoto.ivPhoto3.setImageURI(android.net.Uri.parse(it))
            binding.stepPhoto.ivAdd3.visibility = View.GONE
        } ?: run {
            binding.stepPhoto.ivAdd3.visibility = View.VISIBLE
        }

        binding.stepPhoto.flPhoto1.setOnClickListener {
            pickImage1Launcher.launch("image/*")
        }

        binding.stepPhoto.flPhoto2.setOnClickListener {
            pickImage2Launcher.launch("image/*")
        }

        binding.stepPhoto.flPhoto3.setOnClickListener {
            pickImage3Launcher.launch("image/*")
        }

        binding.stepPhoto.btnNext.setOnClickListener {
            // Validation: At least one photo must be selected
            if (viewModel.photo1Url == null && viewModel.photo2Url == null && viewModel.photo3Url == null) {
                Toast.makeText(this, "Please select at least one photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showStep(1)
        }
    }

    private fun setupNameStep() {
        binding.stepName.root.visibility = View.VISIBLE
        binding.stepName.progressBar.currentStep = 1
        binding.stepName.tvProgress.text = "10%"

        binding.stepName.etName.setText(viewModel.name)

        binding.stepName.ivBack.setOnClickListener {
            showStep(0)
        }

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
        binding.stepGender.tvProgress.text = "20%"

        if (viewModel.gender.isNotEmpty()) {
            val selectedGender = when (viewModel.gender) {
                "male" -> GenderSelector.Gender.MALE
                "female" -> GenderSelector.Gender.FEMALE
                "other" -> GenderSelector.Gender.OTHER
                else -> null
            }
            selectedGender?.let { binding.stepGender.genderSelector.setSelectedGender(it) }
        }

        binding.stepGender.genderSelector.setOnGenderSelectedListener { gender ->
            viewModel.gender = when (gender) {
                GenderSelector.Gender.MALE -> "male"
                GenderSelector.Gender.FEMALE -> "female"
                GenderSelector.Gender.OTHER -> "other"
                else -> ""
            }
        }

        binding.stepGender.ivBack.setOnClickListener {
            showStep(1)
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
        binding.stepBirthday.tvProgress.text = "30%"

        val calendar = Calendar.getInstance()

        if (viewModel.dateOfBirth.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.parse(viewModel.dateOfBirth)
                date?.let { calendar.time = it }
            } catch (e: Exception) {
                calendar.set(2004, Calendar.APRIL, 1)
            }
        } else {
            calendar.set(2004, Calendar.APRIL, 1)
        }

        binding.stepBirthday.tvMonth.text = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
        binding.stepBirthday.tvDay.text = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
        binding.stepBirthday.tvYear.text = calendar.get(Calendar.YEAR).toString()

        binding.stepBirthday.llDatePicker.setOnClickListener {
            showDatePicker()
        }

        binding.stepBirthday.ivBack.setOnClickListener {
            showStep(2)
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
            calendar.get(Calendar.DAY_OF_MONTH),
        )

        datePickerDialog.show()
    }

    private fun setupModeStep() {
        binding.stepMode.root.visibility = View.VISIBLE
        binding.stepMode.progressBar.currentStep = 4
        binding.stepMode.tvProgress.text = "40%"

        val modeChips = listOf(
            binding.stepMode.chipDatingMode to "dating",
            binding.stepMode.chipFriendMode to "friend",
        )

        if (viewModel.mode.isNotEmpty()) {
            when (viewModel.mode) {
                "dating" -> binding.stepMode.chipDatingMode.isChecked = true
                "friend" -> binding.stepMode.chipFriendMode.isChecked = true
            }
        }

        modeChips.forEach { (chip, mode) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.mode = mode
                    modeChips.forEach { (otherChip, _) ->
                        if (otherChip != chip) {
                            otherChip.isChecked = false
                        }
                    }
                }
            }
        }

        binding.stepMode.ivBack.setOnClickListener {
            showStep(3)
        }

        binding.stepMode.btnNext.setOnClickListener {
            if (viewModel.mode.isEmpty()) {
                Toast.makeText(this, "Please select a mode", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.updateProfile()
        }
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
