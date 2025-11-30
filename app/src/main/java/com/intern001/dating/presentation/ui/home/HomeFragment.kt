package com.intern001.dating.presentation.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.data.model.request.LocationRequest
import com.intern001.dating.data.model.request.UpdateProfileRequest
import com.intern001.dating.databinding.FragmentHomeBinding
import com.intern001.dating.domain.model.LocationResult
import com.intern001.dating.domain.usecase.GetLocationUseCase
import com.intern001.dating.domain.usecase.profile.UpdateProfileUseCase
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.discover.DiscoverFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    companion object {
        private var permissionWarningSuppressed = false
        private var locationDisabledWarningSuppressed = false
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var locationWarningDialog: AlertDialog? = null

    @Inject
    lateinit var updateProfileUseCase: UpdateProfileUseCase

    @Inject
    lateinit var getLocationUseCase: GetLocationUseCase

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                refreshLocation()
            } else {
                showLocationWarningDialog(permissionDenied = true)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show bottom navigation when in HomeFragment
        (activity as? MainActivity)?.hideBottomNavigation(false)

        setupListeners()

        // Show DiscoverFragment by default
        if (savedInstanceState == null) {
            showForYou()
        }
    }

    override fun onResume() {
        super.onResume()
        ensureLocationReady()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        binding.btnForYou.setOnClickListener {
            showForYou()
        }

        binding.btnLikedYou.setOnClickListener {
            showLikedYou()
        }
    }

    private fun showForYou() {
        // Update tab UI
        updateTabUI(true)

        // Show DiscoverFragment
        childFragmentManager.beginTransaction()
            .replace(R.id.homeContainer, DiscoverFragment())
            .commit()
    }

    private fun showLikedYou() {
        // Update tab UI
        updateTabUI(false)

        // TODO: Show LikedYouFragment
        // For now, show empty fragment
        childFragmentManager.beginTransaction()
            .replace(R.id.homeContainer, LikedYouFragment())
            .commit()
    }

    fun hideTabBar(hide: Boolean) {
        _binding?.tabContainer?.visibility = if (hide) View.GONE else View.VISIBLE
    }

    private fun ensureLocationReady() {
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
            return
        }
        refreshLocation()
    }

    private fun hasLocationPermission(): Boolean {
        val context = context ?: return false
        val fineGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    private fun refreshLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = getLocationUseCase()) {
                is LocationResult.Success -> {
                    dismissLocationWarning()
                    val location = result.location
                    val request =
                        UpdateProfileRequest(
                            location =
                            LocationRequest(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                city = location.city,
                                country = location.country,
                                coordinates = listOf(location.longitude, location.latitude),
                            ),
                            city = location.city,
                            country = location.country,
                        )
                    updateProfileUseCase(request)
                        .onSuccess {
                            Log.d(
                                "HomeFragment",
                                "GPS location synced: ${location.latitude}, ${location.longitude}",
                            )
                        }
                        .onFailure { error ->
                            Log.e("HomeFragment", "Failed to sync location", error)
                        }
                }
                is LocationResult.LocationDisabled -> {
                    showLocationWarningDialog(permissionDenied = false)
                }
                is LocationResult.PermissionDenied -> {
                    showLocationWarningDialog(permissionDenied = true)
                }
                is LocationResult.Error -> {
                    Log.e("HomeFragment", "Error getting location", result.exception)
                }
                LocationResult.Loading -> Unit
            }
        }
    }

    private fun dismissLocationWarning() {
        locationWarningDialog?.dismiss()
        locationWarningDialog = null
        permissionWarningSuppressed = false
        locationDisabledWarningSuppressed = false
    }

    private fun showLocationWarningDialog(
        permissionDenied: Boolean,
        force: Boolean = false,
    ) {
        if (locationWarningDialog?.isShowing == true && !force) return

        val context = context ?: return
        val messageRes =
            if (permissionDenied) {
                R.string.location_permission_message
            } else {
                R.string.location_warning_message
            }

        if (!force) {
            val alreadySuppressed =
                if (permissionDenied) {
                    permissionWarningSuppressed
                } else {
                    locationDisabledWarningSuppressed
                }
            if (alreadySuppressed) return
        }

        if (permissionDenied) {
            permissionWarningSuppressed = true
        } else {
            locationDisabledWarningSuppressed = true
        }

        val dialog =
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.location_warning_title)
                .setMessage(messageRes)
                .setPositiveButton(R.string.action_enable_location) { _, _ ->
                    if (permissionDenied) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    } else {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()

        dialog.setOnDismissListener {
            if (locationWarningDialog === dialog) {
                locationWarningDialog = null
            }
        }

        locationWarningDialog = dialog
        dialog.show()
    }

    private fun updateTabUI(isForYouSelected: Boolean) {
        if (isForYouSelected) {
            // For You selected
            binding.btnForYou.apply {
                setBackgroundColor(context.getColor(R.color.bottom_nav_selected))
                setTextColor(context.getColor(R.color.white))
            }
            binding.btnLikedYou.apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setTextColor(context.getColor(R.color.bottom_nav_unselected))
            }
        } else {
            // Liked You selected
            binding.btnForYou.apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setTextColor(context.getColor(R.color.bottom_nav_unselected))
            }
            binding.btnLikedYou.apply {
                setBackgroundColor(context.getColor(R.color.bottom_nav_selected))
                setTextColor(context.getColor(R.color.white))
            }
        }
    }
}
