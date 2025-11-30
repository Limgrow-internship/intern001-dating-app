package com.intern001.dating.presentation.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.intern001.dating.databinding.FragmentLikedYouBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope

class LikedYouFragment : Fragment() {

    private var _binding: FragmentLikedYouBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MatchStatusViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLikedYouBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fakeTargetUserId = "67ae123abca123"

        viewModel.loadMatchStatus(fakeTargetUserId)

        viewModel.status.observe(viewLifecycleOwner) { status ->
            if (status != null) {

                // bạn update UI ở đây
                println("Matched: ${status.matched}")
                println("User liked: ${status.userLiked}")
                println("Target liked: ${status.targetLiked}")
                println("Name: ${status.targetProfile?.displayName}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
