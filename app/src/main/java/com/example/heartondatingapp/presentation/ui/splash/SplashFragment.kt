package com.example.heartondatingapp.presentation.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.heartondatingapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide bottom navigation during splash
        (activity as? com.example.heartondatingapp.MainActivity)?.hideBottomNavigation(true)

        // Navigate to home after a short delay
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000) // 2 second delay
            findNavController().navigate(R.id.action_splash_to_home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show bottom navigation when leaving splash
        (activity as? com.example.heartondatingapp.MainActivity)?.hideBottomNavigation(false)
    }
}
