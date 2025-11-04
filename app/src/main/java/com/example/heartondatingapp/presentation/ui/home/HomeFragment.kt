package com.example.heartondatingapp.presentation.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.heartondatingapp.R
import com.example.heartondatingapp.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Example: Using NavigationManager to navigate
        // Uncomment to test navigation:

        // Navigate to profile detail
        // view.findViewById<View>(R.id.someButton)?.setOnClickListener {
        //     navigationManager.navigateToDynamicRoute(
        //         navController,
        //         "profile_detail/userId123"
        //     )
        // }

        // Or navigate using action ID
        // view.findViewById<View>(R.id.someButton)?.setOnClickListener {
        //     navController.navigate(R.id.action_home_to_profileDetail)
        // }

        // Or navigate back
        // view.findViewById<View>(R.id.backButton)?.setOnClickListener {
        //     navigateUp()
        // }
    }
}
