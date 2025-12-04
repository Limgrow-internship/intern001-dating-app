package com.intern001.dating.presentation.ui.profile.edit

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.intern001.dating.R
import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.databinding.FragmentEditProfileBinding
import com.intern001.dating.databinding.ItemPhotoProfileBinding
import com.intern001.dating.domain.model.UpdateProfile
import com.intern001.dating.domain.usecase.photo.UploadPhotoUseCase
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.text.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class EditProfileFragment : BaseFragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var uploadPhotoUseCase: UploadPhotoUseCase

    private val viewModel: EditProfileViewModel by viewModels()

    private lateinit var photos: List<ItemPhotoProfileBinding>
    private val photoUris = MutableList<Uri?>(6) { null }
    private val photoUrls = MutableList<String?>(6) { null }
    private val photoIds = MutableList<String?>(6) { null } // Store photo IDs from server
    private var currentPhotoIndex = 0

    private val selectedGoals = mutableSetOf<String>()
    private val selectedInterests = mutableSetOf<String>()

    // Store profile data to preserve firstName, lastName, dateOfBirth, gender when updating
    private var currentProfile: UpdateProfile? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { onPhotoSelected(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        photos = listOf(
            binding.itemPhoto1,
            binding.itemPhoto2,
            binding.itemPhoto3,
            binding.itemPhoto4,
            binding.itemPhoto5,
            binding.itemPhoto6,
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPhotoClick()
        setupGoalClick()
        setupInterestClick()
        setupSaveButton()
        observeUpdateState()
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModel.getUserProfile()

        lifecycleScope.launch {
            viewModel.userProfileState.collect { state ->
                when (state) {
                    is EditProfileViewModel.UiState.Success ->
                        bindProfileData(state.data)

                    is EditProfileViewModel.UiState.Error ->
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()

                    else -> Unit
                }
            }
        }
    }

    private fun bindProfileData(profile: UpdateProfile) {
        currentProfile = profile

        binding.includeAbout.etIntroduce.setText(profile.bio ?: "")

        profile.photos.forEachIndexed { index, photo ->
            if (index < 6) {
                photoUrls[index] = photo.url
                photoIds[index] = photo.id
                photoUris[index] = null
            }
        }
        updatePhotoViews()

        val info = binding.includeInfo

        val fullName = listOfNotNull(profile.firstName, profile.lastName).joinToString(" ")
        info.etName.setText(fullName)

        profile.dateOfBirth?.let { date ->
            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            info.etBirthday.setText(formatted)
        } ?: info.etBirthday.setText("")

        info.etBirthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            profile.dateOfBirth?.let { calendar.time = it }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val dateString =
                    String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                info.etBirthday.setText(dateString)
            }, year, month, day).show()
        }

        val genders = arrayOf("male", "female", "other")
        if (profile.gender != null && genders.contains(profile.gender)) {
            info.etGender.setText(profile.gender)
        } else {
            info.etGender.setText("")
        }

        info.etGender.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Select Gender")
                .setItems(genders) { _, which ->
                    info.etGender.setText(genders[which])
                }
                .show()
        }

        val g = binding.includeGoals

        val goalMap = mapOf(
            g.tvGoalSerious.text.toString() to g.tvGoalSerious,
            g.tvGoalFriends.text.toString() to g.tvGoalFriends,
            g.tvGoalCasual.text.toString() to g.tvGoalCasual,
            g.tvGoalVibing.text.toString() to g.tvGoalVibing,
            g.tvGoalOpen.text.toString() to g.tvGoalOpen,
            g.tvGoalFiguring.text.toString() to g.tvGoalFiguring,
        )

        profile.goals.forEach { goal ->
            goalMap[goal]?.let { toggleGoalSelection(it) }
        }

        selectedGoals.clear()
        selectedGoals.addAll(profile.goals)

        val i = binding.includeInterests

        val interestMap = mapOf(
            i.tvInterestMusic.text.toString() to i.tvInterestMusic,
            i.tvInterestPhotography.text.toString() to i.tvInterestPhotography,
            i.tvInterestTravel.text.toString() to i.tvInterestTravel,
            i.tvInterestDeepTalks.text.toString() to i.tvInterestDeepTalks,
            i.tvInterestReadBook.text.toString() to i.tvInterestReadBook,
            i.tvInterestWalking.text.toString() to i.tvInterestWalking,
            i.tvInterestPets.text.toString() to i.tvInterestPets,
            i.tvInterestCooking.text.toString() to i.tvInterestCooking,
        )

        profile.interests.forEach { interest ->
            interestMap[interest]?.let { toggleInterestSelection(it) }
        }

        selectedInterests.clear()
        selectedInterests.addAll(profile.interests)

        val details = binding.includeDetails
        details.comboEducation.setText(profile.education ?: "", false)
        details.comboAddress.setText(profile.city ?: profile.location?.city ?: "", false)
        details.etHeight.setText(profile.height?.toString() ?: "")
        details.etWeight.setText(profile.weight?.toString() ?: "")
        details.comboZodiac.setText(profile.zodiacSign ?: "", false)

        val jobs = resources.getStringArray(R.array.job_list).toList()
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, jobs)
        details.comboJob.setAdapter(adapter)

        val currentJob = profile.job ?: profile.occupation ?: ""
        details.comboJob.setText(currentJob, false)

        details.comboJob.setOnClickListener {
            details.comboJob.showDropDown()
        }

        val educations = resources.getStringArray(R.array.university_list_vietnam).toList()
        val adapterEdu = ArrayAdapter(requireContext(), R.layout.dropdown_item, educations)
        details.comboEducation.setAdapter(adapterEdu)

        val currentEducation = profile.education ?: ""
        details.comboEducation.setText(currentEducation, false)

        details.comboEducation.setOnClickListener {
            details.comboEducation.showDropDown()
        }

        val zodiacs = resources.getStringArray(R.array.zodiac_list).toList()
        val adapterZodiacs = ArrayAdapter(requireContext(), R.layout.dropdown_item, zodiacs)
        details.comboZodiac.setAdapter(adapterZodiacs)

        val currentZodiac = profile.zodiacSign ?: ""
        details.comboZodiac.setText(currentZodiac, false)

        details.comboZodiac.setOnClickListener {
            details.comboZodiac.showDropDown()
        }

        val addressList = resources.getStringArray(R.array.vietnam_provinces_cities).toList()
        val adapterAddress = ArrayAdapter(requireContext(), R.layout.dropdown_item, addressList)
        details.comboAddress.setAdapter(adapterAddress)

        val currentAddress = profile.city ?: ""
        details.comboAddress.setText(currentAddress, false)

        details.comboAddress.setOnClickListener {
            details.comboAddress.showDropDown()
        }

        details.comboAddress.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val input = details.comboAddress.text.toString()
                if (!addressList.contains(input)) {
                    details.comboAddress.setText("")
                    Toast.makeText(requireContext(), "Vui lòng chọn tỉnh/thành phố hợp lệ", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val question = binding.includeQuestions

        val whatQuestions = resources.getStringArray(R.array.open_question_list_what)
            .map {
                val parts = it.split("|", limit = 2)
                parts[0] to parts[1]
            }

        val idealQuestions = resources.getStringArray(R.array.open_question_list_ideal)
            .map {
                val parts = it.split("|", limit = 2)
                parts[0] to parts[1]
            }

        val whatDisplayList = whatQuestions.map { it.second }
        val idealDisplayList = idealQuestions.map { it.second }

        val answers = profile.openQuestionAnswers ?: emptyMap()

        Log.d("QA_DEBUG", "Loaded answers = $answers")

        question.comboWhat.setAdapter(
            ArrayAdapter(requireContext(), R.layout.dropdown_item, whatDisplayList),
        )
        question.comboWeekend.setAdapter(
            ArrayAdapter(requireContext(), R.layout.dropdown_item, idealDisplayList),
        )

        findExactKeyFromServer(answers.keys, whatQuestions)?.let { (displayText, key) ->
            question.comboWhat.setText(displayText, false)
            question.etWhat.setText(answers[key])
        } ?: run {
            question.comboWhat.setText("", false)
            question.etWhat.setText("")
        }

        findExactKeyFromServer(answers.keys, idealQuestions)?.let { (displayText, key) ->
            question.comboWeekend.setText(displayText, false)
            question.etWeekend.setText(answers[key])
        } ?: run {
            question.comboWeekend.setText("", false)
            question.etWeekend.setText("")
        }

        question.comboWhat.setOnClickListener { question.comboWhat.showDropDown() }
        question.comboWeekend.setOnClickListener { question.comboWeekend.showDropDown() }
    }
    private fun findExactKeyFromServer(
        serverKeys: Set<String>,
        questionList: List<Pair<String, String>>,
    ): Pair<String, String>? {
        for (key in serverKeys) {
            val found = questionList.find { it.first == key }
            if (found != null) {
                val (k, text) = found
                return text to k
            }
        }
        return null
    }

    private fun setupPhotoClick() {
        photos.forEachIndexed { index, item ->
            item.ivPhoto.setOnClickListener {
                currentPhotoIndex = index
                pickImageLauncher.launch("image/*")
            }
        }
    }

    private fun onPhotoSelected(uri: Uri) {
        photoUris[currentPhotoIndex] = uri
        updatePhotoViews()

        lifecycleScope.launch {
            uploadPhoto(uri, currentPhotoIndex)
        }
    }

    private suspend fun uploadPhoto(uri: Uri, index: Int) {
        withContext(Dispatchers.IO) {
            // Upload photo via Photo Management API (not just Cloudinary)
            val result = uploadPhotoUseCase(uri, type = "gallery")

            withContext(Dispatchers.Main) {
                result.onSuccess { photo ->
                    // Store both URL and ID
                    photoUrls[index] = photo.url
                    photoIds[index] = photo.id
                    // Update photo view immediately
                    updatePhotoViews()
                    Toast.makeText(requireContext(), "Photo ${index + 1} uploaded successfully", Toast.LENGTH_SHORT).show()

                    // Reload profile to get updated photos from server
                    viewModel.getUserProfile()
                }.onFailure { e ->
                    Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updatePhotoViews() {
        photos.forEachIndexed { index, item ->
            val localUri = photoUris[index]
            val serverUrl = photoUrls[index]

            when {
                localUri != null ->
                    item.ivPhoto.setImageURI(localUri)

                !serverUrl.isNullOrEmpty() ->
                    Glide.with(requireContext())
                        .load(serverUrl)
                        .centerCrop()
                        .into(item.ivPhoto)
            }
        }
    }

    private fun setupGoalClick() {
        val g = binding.includeGoals

        val goalViews = listOf(
            g.tvGoalSerious,
            g.tvGoalFriends,
            g.tvGoalCasual,
            g.tvGoalVibing,
            g.tvGoalOpen,
            g.tvGoalFiguring,
        )

        goalViews.forEach { goal ->
            goal.setOnClickListener { toggleGoalSelection(goal) }
        }
    }

    private fun toggleGoalSelection(goal: TextView) {
        val text = goal.text.toString()

        if (selectedGoals.contains(text)) {
            selectedGoals.remove(text)
            goal.setBackgroundResource(R.drawable.chip_unselected)
            goal.setTextColor(Color.BLACK)
        } else {
            selectedGoals.add(text)
            goal.setBackgroundResource(R.drawable.chip_selected)
            goal.setTextColor(Color.WHITE)
        }
    }

    private fun setupInterestClick() {
        val i = binding.includeInterests

        val interestViews = listOf(
            i.tvInterestMusic,
            i.tvInterestPhotography,
            i.tvInterestTravel,
            i.tvInterestDeepTalks,
            i.tvInterestReadBook,
            i.tvInterestWalking,
            i.tvInterestPets,
            i.tvInterestCooking,
        )

        interestViews.forEach { interest ->
            interest.setOnClickListener { toggleInterestSelection(interest) }
        }
    }

    private fun toggleInterestSelection(interest: TextView) {
        val text = interest.text.toString()

        if (selectedInterests.contains(text)) {
            selectedInterests.remove(text)
            interest.setBackgroundResource(R.drawable.chip_unselected)
            interest.setTextColor(Color.BLACK)
        } else {
            selectedInterests.add(text)
            interest.setBackgroundResource(R.drawable.chip_selected)
            interest.setTextColor(Color.WHITE)
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val bio = binding.includeAbout.etIntroduce.text.toString().trim()

            // Personal details
            val details = binding.includeDetails
            val job = details.comboJob.text.toString().trim().takeIf { it.isNotEmpty() }
            val education = details.comboEducation.text.toString().trim().takeIf { it.isNotEmpty() }
            val address = details.comboAddress.text.toString().trim().takeIf { it.isNotEmpty() }
            val height = details.etHeight.text.toString().trim().toIntOrNull()
            val weight = details.etWeight.text.toString().trim().toIntOrNull()
            val zodiacSign = details.comboZodiac.text.toString().trim().takeIf { it.isNotEmpty() }

            val info = binding.includeInfo
            val name = info.etName.text.toString().trim()
            val birthday = info.etBirthday.text.toString().trim()
            val gender = info.etGender.text.toString().trim()

            val profile = currentProfile

            val whatQuestions = resources.getStringArray(R.array.open_question_list_what)
                .map {
                    val parts = it.split("|", limit = 2)
                    parts[0] to parts[1] // key → text
                }

            val idealQuestions = resources.getStringArray(R.array.open_question_list_ideal)
                .map {
                    val parts = it.split("|", limit = 2)
                    parts[0] to parts[1]
                }

            val question = binding.includeQuestions
            val openQuestionMap = mutableMapOf<String, String>()

            // =============================
            // HANDLE WHAT QUESTION
            // =============================
            val selectedWhatText = question.comboWhat.text.toString()
            val answerWhat = question.etWhat.text.toString().trim()

            if (selectedWhatText.isNotEmpty() && answerWhat.isNotEmpty()) {
                val entry = whatQuestions.find { it.second == selectedWhatText }
                if (entry != null) {
                    val key = entry.first // q1, q2, q3…
                    openQuestionMap[key] = answerWhat
                }
            }

            // =============================
            // HANDLE IDEAL QUESTION
            // =============================
            val selectedIdealText = question.comboWeekend.text.toString()
            val answerIdeal = question.etWeekend.text.toString().trim()

            if (selectedIdealText.isNotEmpty() && answerIdeal.isNotEmpty()) {
                val entry = idealQuestions.find { it.second == selectedIdealText }
                if (entry != null) {
                    val key = entry.first // i1, i2, i3…
                    openQuestionMap[key] = answerIdeal
                }
            }

            val request = UpdateProfileRequest(
                firstName = profile?.firstName,
                lastName = profile?.lastName,
                dateOfBirth = birthday,
                gender = gender,
                bio = bio,
                goals = selectedGoals.toList(),
                interests = selectedInterests.toList(),
                job = job,
                occupation = job,
                education = education,
                city = address,
                zodiacSign = zodiacSign,
                height = height,
                weight = weight,
                displayName = profile?.displayName,
                relationshipMode = profile?.relationshipMode,
                openQuestionAnswers =
                if (openQuestionMap.isEmpty()) emptyMap() else openQuestionMap,
            )
            Log.e("DEBUG_SAVE", "Sending request = ${Gson().toJson(request)}")

            viewModel.updateUserProfile(request)
        }
    }

    private fun observeUpdateState() {
        lifecycleScope.launch {
            viewModel.updateProfileState.collect { state ->
                when (state) {
                    is EditProfileViewModel.UiState.Success<*> -> {
                        Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                        // Reload profile to get updated data including photos
                        viewModel.getUserProfile()
                    }

                    is EditProfileViewModel.UiState.Error ->
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()

                    else -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
