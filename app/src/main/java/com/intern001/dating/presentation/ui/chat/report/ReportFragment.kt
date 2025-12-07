package com.intern001.dating.presentation.ui.chat.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.intern001.dating.databinding.FragmentReportBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment

class ReportFragment : BaseFragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

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
        ReportAdapter.ReportItem("other", "Other")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ReportAdapter(reportItems) { selected ->
            Toast.makeText(requireContext(), "Selected: ${selected.title}", Toast.LENGTH_SHORT).show()
            // TODO: Gửi API, lưu DB...
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

}
