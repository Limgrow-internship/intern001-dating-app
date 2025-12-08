package com.intern001.dating.presentation.ui.chat.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.intern001.dating.databinding.FragmentReportBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.report.ReportViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ReportFragment : BaseFragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by viewModels()
    private val reportedUserId by lazy {
        arguments?.getString("targetUserId") ?: ""
    }

    private var selectedReason: String? = null

    private val reportItems = listOf(
        ReportAdapter.ReportItem("fake_profile", "Fake profile"),
        ReportAdapter.ReportItem("inappropriate_content", "Inappropriate content"),
        ReportAdapter.ReportItem("harassment_bullying", "Harassment or bullying"),
        ReportAdapter.ReportItem("scam_behavior", "Scams or suspicious behavior"),
        ReportAdapter.ReportItem("sexual_content", "Sexual content"),
        ReportAdapter.ReportItem("underage_user", "Underage user"),
        ReportAdapter.ReportItem("hate_speech", "Hate speech"),
        ReportAdapter.ReportItem("violence", "Violence or dangerous behavior"),
        ReportAdapter.ReportItem("spam", "Spam"),
        ReportAdapter.ReportItem("other", "Other"),
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ReportAdapter(reportItems) { selected ->
                selectedReason = selected.key
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (selectedReason == null) {
                Toast.makeText(requireContext(), "Select reason", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            android.util.Log.d(
                "ReportFragment",
                "Submitting report: matchedUserId=$reportedUserId reason=$selectedReason",
            )

            viewModel.submitReport(
                userIdIsReported = reportedUserId,
                reason = selectedReason!!,
            )
        }

        observeStates()
    }

    private fun observeStates() {
        lifecycleScope.launchWhenStarted {
            viewModel.reportResult.collectLatest {
                it?.let {
                    Toast.makeText(requireContext(), "Reported", Toast.LENGTH_SHORT).show()
                    viewModel.clearResult()
                    findNavController().popBackStack()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest {
                it?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.loading.collectLatest { isLoading ->
                binding.progressBar.visibility =
                    if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
