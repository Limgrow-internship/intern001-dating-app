package com.intern001.dating.presentation.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.databinding.FragmentChatScreenBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.MessageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatDetailFragment : BaseFragment() {
    private val viewModel: ChatViewModel by viewModels()
    private var matchId: String = ""
    private var matchedUserName: String? = null
    private lateinit var adapter: MessageAdapter

    private var _binding: FragmentChatScreenBinding? = null
    private val binding get() = _binding!!

    private val tokenManager by lazy { TokenManager(requireContext()) }

    private suspend fun getMyUserIdAsync(): String {
        return tokenManager.getUserIdAsync() ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            matchId = it.getString("matchId", "")
            matchedUserName = it.getString("matchedUserName")
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.tvUserName.text = matchedUserName ?: "Chat"

        lifecycleScope.launch {
            val myUserId = getMyUserIdAsync()

            adapter = MessageAdapter(myUserId)
            binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMessages.adapter = adapter

            binding.btnSend.setOnClickListener {
                val content = binding.edtMessage.text.toString().trim()
                if (content.isNotEmpty()) {
                    lifecycleScope.launch {
                        val confirmedUserId = getMyUserIdAsync()
                        val message = MessageModel(
                            senderId = confirmedUserId,
                            matchId = matchId,
                            message = content,
                        )
                        viewModel.sendMessage(message)
                        binding.edtMessage.setText("")
                    }
                }
            }

            lifecycleScope.launch {
                viewModel.messages.collectLatest { msgs ->
                    adapter.setMessages(msgs)
                    binding.rvMessages.scrollToPosition(msgs.size - 1)
                }
            }

            viewModel.fetchHistory(matchId)
        }
    }
}
