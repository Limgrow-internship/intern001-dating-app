package com.intern001.dating.presentation.ui.profile.edit

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.intern001.dating.data.repository.AuthRepositoryImpl
import com.intern001.dating.databinding.FragmentEditProfileBinding
import com.intern001.dating.databinding.ItemPhotoProfileBinding
import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.domain.model.UpdateProfile
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.intern001.dating.R

@AndroidEntryPoint
class EditProfileFragment : BaseFragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var authRepository: AuthRepositoryImpl

    private val viewModel: EditProfileViewModel by viewModels()

    private lateinit var photos: List<ItemPhotoProfileBinding>
    private val photoUris = MutableList<Uri?>(6) { null }
    private val photoUrls = MutableList<String?>(6) { null }
    private var currentPhotoIndex = 0

    private val selectedGoals = mutableSetOf<String>()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { onPhotoSelected(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        photos = listOf(
            binding.itemPhoto1,
            binding.itemPhoto2,
            binding.itemPhoto3,
            binding.itemPhoto4,
            binding.itemPhoto5,
            binding.itemPhoto6
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPhotoClick()
        setupGoalClick()
        setupSaveButton()
        observeUpdateState()
        loadUserProfile()
    }


    private fun loadUserProfile() {
        viewModel.getUserProfile()

        lifecycleScope.launch {
            viewModel.userProfileState.collect { state ->
                when (state) {
                    is EditProfileViewModel.UiState.Success -> bindProfileData(state.data)

                    is EditProfileViewModel.UiState.Error ->
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()

                    else -> Unit
                }
            }
        }
    }

    private fun bindProfileData(profile: UpdateProfile) {

        binding.includeAbout.etIntroduce.setText(profile.bio ?: "")

        profile.photos?.forEachIndexed { index, url ->
            if (index < 6) {
                photoUrls[index] = url
                photoUris[index] = null
            }
        }
        updatePhotoViews()

        val g = binding.includeGoals

        val goalMap = mapOf(
            "Serious relationship" to g.tvGoalSerious,
            "New friends" to g.tvGoalFriends,
            "Something casual" to g.tvGoalCasual,
            "Just vibing" to g.tvGoalVibing,
            "Open to anything" to g.tvGoalOpen,
            "Still figuring it out" to g.tvGoalFiguring
        )

        profile.goals?.split(",")?.map { it.trim() }?.forEach { goal ->
            goalMap[goal]?.let { toggleGoalSelection(it) }
        }
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
            val result = authRepository.uploadImage(uri)

            withContext(Dispatchers.Main) {
                result.onSuccess { url ->
                    photoUrls[index] = url
                    Toast.makeText(requireContext(), "Ảnh ${index + 1} đã upload", Toast.LENGTH_SHORT).show()
                }.onFailure { e ->
                    Toast.makeText(requireContext(), "Upload thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
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

                else ->
                    item.ivPhoto.setImageResource(R.drawable.co4la)
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
            g.tvGoalFiguring
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

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val bio = binding.includeAbout.etIntroduce.text.toString().trim()
            val finalPhotos = photoUrls.map { it ?: "" }

            val request = UpdateProfileRequest(
                photos = finalPhotos,
                bio = bio,
                goals = selectedGoals.joinToString(",")
            )

            viewModel.updateUserProfile(request)
        }
    }

    private fun observeUpdateState() {
        lifecycleScope.launch {
            viewModel.updateProfileState.collect { state ->
                when (state) {
                    is EditProfileViewModel.UiState.Success<*> ->
                        Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show()

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
