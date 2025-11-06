package com.intern001.dating.presentation.ui.onboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import com.intern001.dating.presentation.common.viewmodel.BaseFragment

class OnboardSecondFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboard_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? MainActivity)?.hideBottomNavigation(true)
        view.findViewById<LinearLayout>(R.id.btnContinue).setOnClickListener {
            findNavController().navigate(R.id.action_onboardSecond_to_onboardThird)
        }
    }
    override fun onDestroyView() {
        (activity as? MainActivity)?.hideBottomNavigation(true)
        super.onDestroyView()
    }
}
