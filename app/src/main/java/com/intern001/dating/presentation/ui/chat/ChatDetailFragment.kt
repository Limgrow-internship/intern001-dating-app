package com.intern001.dating.presentation.ui.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.databinding.FragmentChatScreenBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.MessageAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
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

    private val emojiList = listOf(
        "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊", "😋", "😎", "😍", "😘", "😗", "😙", "😚", "☺️", "🙂", "🤗", "🤩", "🤔", "🤨",
        "😐", "😑", "😶", "🙄", "😏", "😣", "😥", "😮", "🤐", "😯", "😪", "😫", "😴", "😌", "😛", "😜", "😝", "🤤", "😒", "😓", "😔", "😕", "🙃",
        "🤑", "😲", "☹️", "🙁", "😖", "😞", "😟", "😤", "😢", "😭", "😦", "😧", "😨", "😩", "🤯", "😬", "😰", "😱", "😳", "🤪", "😵", "😡",
        "😠", "🤬", "😷", "🤒", "🤕", "🤢", "🤮", "🤧", "😇", "🥳", "🥰", "🤠", "🤡", "🥺",
    )

    private var recorder: MediaRecorder? = null
    private var audioFilePath: String = ""
    private var isRecording = false
    private var recordStartTime: Long = 0

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
        recorder?.release()
        recorder = null
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

        val emojiRecycler = RecyclerView(requireContext()).apply {
            layoutManager = GridLayoutManager(requireContext(), 8)
            adapter = EmojiAdapter(emojiList) { emoji ->
                val current = binding.edtMessage.text.toString()
                val selection = binding.edtMessage.selectionStart
                val newText = StringBuilder(current).apply { insert(selection, emoji) }.toString()
                binding.edtMessage.setText(newText)
                binding.edtMessage.setSelection(selection + emoji.length)
            }
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            isVerticalScrollBarEnabled = false
            setPadding(16, 8, 16, 20)
        }
        if (binding.emojiContainer.childCount == 0) {
            binding.emojiContainer.addView(emojiRecycler)
        }

        binding.btnEmoji.setOnClickListener {
            val isShowing = binding.emojiContainer.visibility == View.VISIBLE
            if (isShowing) {
                binding.emojiContainer.visibility = View.GONE
            } else {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.edtMessage.windowToken, 0)
                binding.emojiContainer.visibility = View.VISIBLE
            }
        }
        binding.edtMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.emojiContainer.visibility = View.GONE
        }

        // ===== Voice Recording & gửi message voice =====
        binding.btnVoice.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            123,
                        )
                        return@setOnTouchListener true
                    }
                    val started = startRecording()
                    setRecordingUI(started)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val sent = stopRecording()
                    setRecordingUI(false)
                    if (sent) {
                        lifecycleScope.launch {
                            val confirmedUserId = getMyUserIdAsync()
                            val duration = getAudioDurationSeconds(audioFilePath)
                            viewModel.sendVoiceMessage(
                                matchId = matchId,
                                senderId = confirmedUserId,
                                localAudioPath = audioFilePath,
                                duration = duration,
                            )
                        }
                    } else {
                        Toast.makeText(context, "Bản ghi quá ngắn!", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
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
    private fun setRecordingUI(isRecording: Boolean) {
        binding.tvRecording.visibility = if (isRecording) View.VISIBLE else View.GONE
    }
    private fun startRecording(): Boolean {
        return try {
            val audioDir = requireContext().externalCacheDir
            val outputFile = File(audioDir, "audio_${System.currentTimeMillis()}.m4a")
            audioFilePath = outputFile.absolutePath

            recorder = MediaRecorder()
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }
            isRecording = true
            recordStartTime = System.currentTimeMillis()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun stopRecording(): Boolean {
        if (isRecording) {
            val recordDuration = System.currentTimeMillis() - recordStartTime
            if (recordDuration < 300) {
                try {
                    recorder?.reset()
                } catch (_: Exception) {}
                recorder?.release()
                recorder = null
                isRecording = false
                File(audioFilePath).delete()
                return false
            }
            try {
                recorder?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                File(audioFilePath).delete()
                isRecording = false
                recorder = null
                return false
            }
            recorder = null
            isRecording = false
            return File(audioFilePath).exists() && File(audioFilePath).length() > 0
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val started = startRecording()
            setRecordingUI(started)
        }
    }
    private fun getAudioDurationSeconds(path: String?): Int {
        if (path == null || !File(path).exists()) return 0
        try {
            val player = android.media.MediaPlayer()
            player.setDataSource(path)
            player.prepare()
            val durationMs = player.duration
            player.release()
            return durationMs / 1000
        } catch (_: Exception) {}
        return 0
    }
}
