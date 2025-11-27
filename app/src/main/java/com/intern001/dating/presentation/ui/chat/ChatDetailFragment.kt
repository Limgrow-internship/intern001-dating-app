package com.intern001.dating.presentation.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intern001.dating.R
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.MessageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatDetailFragment : BaseFragment() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var matchId: String
    private lateinit var adapter: MessageAdapter
    private var matchedUserName: String? = null

    // Sử dụng TokenManager chuẩn
    private val tokenManager by lazy { TokenManager(requireContext()) }

    // Thêm hàm lấy userId dạng suspend
    private suspend fun getMyUserIdAsync(): String {
        return tokenManager.getUserIdAsync() ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Lấy matchId từ argument navigation
        arguments?.let {
            matchId = it.getString("matchId", "")
            matchedUserName = it.getString("matchedUserName")
        }

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Gán tên user lên UI
        view.findViewById<TextView>(R.id.tvUserName).text = matchedUserName ?: "Chat"

        lifecycleScope.launch {
            val myUserId = getMyUserIdAsync()
            Log.d("DEBUG", "myUserId = $myUserId")
            // Nếu userId rỗng, nên show lỗi hoặc chuyển login!

            viewModel = ViewModelProvider(this@ChatDetailFragment)[ChatViewModel::class.java]
            val rvMessages = view.findViewById<RecyclerView>(R.id.rvMessages)
            adapter = MessageAdapter(myUserId)
            rvMessages.layoutManager = LinearLayoutManager(requireContext())
            rvMessages.adapter = adapter

            // Set nút send để gửi message với userId chính xác
            val edtMessage = view.findViewById<EditText>(R.id.edtMessage)
            val btnSend = view.findViewById<ImageButton>(R.id.btnSend)

            btnSend.setOnClickListener {
                val content = edtMessage.text.toString().trim()
                if (content.isNotEmpty()) {
                    // Gửi message với userId luôn đúng giá trị
                    lifecycleScope.launch {
                        val confirmedUserId = getMyUserIdAsync()
                        val message = MessageModel(
                            senderId = confirmedUserId,
                            roomId = matchId,
                            message = content,
                        )
                        viewModel.sendMessage(message)
                        edtMessage.setText("")
                    }
                }
            }

            viewModel.messages.observe(viewLifecycleOwner) { msgs ->
                adapter.setMessages(msgs)
                rvMessages.scrollToPosition(msgs.size - 1)
            }

            viewModel.fetchHistory(matchId)
        }
    }
}
