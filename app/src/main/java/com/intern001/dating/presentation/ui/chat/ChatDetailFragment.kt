package com.intern001.dating.presentation.ui.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.bumptech.glide.Glide
import com.intern001.dating.R
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.databinding.FragmentChatScreenBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.ui.chat.AIConstants
import com.intern001.dating.presentation.ui.chat.MessageAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@AndroidEntryPoint
class ChatDetailFragment : BaseFragment() {
    private val viewModel: ChatViewModel by viewModels()
    private var matchId: String = ""
    private var matchedUserName: String? = null
    private lateinit var adapter: MessageAdapter

    private var _binding: FragmentChatScreenBinding? = null
    private val binding get() = _binding!!
    private val tokenManager by lazy { TokenManager(requireContext()) }

    private var targetUserId: String = ""

    private val CAMERA_REQUEST_CODE = 124

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

    private var isAIConversation = false

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handlePickedImage(it) }
    }

    private var cameraImageUri: Uri? = null
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                cameraImageUri?.let { showPreviewImage(it) }
            } else {
                cameraImageUri = null
            }
        }

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
        requireActivity().window.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE,
        )
        arguments?.let {
            matchId = it.getString("matchId", "")
            matchedUserName = it.getString("matchedUserName")
            targetUserId = it.getString("targetUserId", "")
        }

        isAIConversation = AIConstants.isAIUser(targetUserId)

        if (isAIConversation && matchedUserName.isNullOrBlank()) {
            matchedUserName = AIConstants.AI_FAKE_NAME
        }

        setupTypingIndicatorAnimation()

        lifecycleScope.launch {
            val myUserId = getMyUserIdAsync()
            viewModel.currentUserId = myUserId
            viewModel.currentMatchId = matchId
            viewModel.isAIConversation = isAIConversation

            viewModel.initializeSocket()
            kotlinx.coroutines.delay(500)
            viewModel.joinChatRoom(matchId, myUserId, isAIConversation)

            android.util.Log.d("ChatDetailFragment", "Initialized socket and joined room: matchId=$matchId, userId=$myUserId, isAI=$isAIConversation")
        }

        if (!isAIConversation) {
            viewModel.fetchMatchStatus(targetUserId)
        } else {
            binding.messageInputLayout.visibility = View.VISIBLE
            binding.tvUnmatched.visibility = View.GONE
        }
        lifecycleScope.launch {
            combine(viewModel.matchStatus, viewModel.blockerId) { status, blockerId ->
                Pair(status, blockerId)
            }.collectLatest { (status, blockerId) ->
                val myUserId = getMyUserIdAsync()
                _binding?.let { binding ->
                    if (status == "unmatched") {
                        binding.messageInputLayout.visibility = View.GONE
                        binding.tvUnmatched.visibility = View.VISIBLE
                        binding.tvBlocked.visibility = View.GONE
                        binding.btnMore.visibility = View.GONE
                    } else if (status == "blocked") {
                        if (blockerId == myUserId) {
                            binding.messageInputLayout.visibility = View.GONE
                            binding.tvBlocked.visibility = View.VISIBLE
                        } else {
                            binding.messageInputLayout.visibility = View.VISIBLE
                            binding.tvBlocked.visibility = View.GONE
                        }
                    } else {
                        binding.messageInputLayout.visibility = View.VISIBLE
                        binding.tvUnmatched.visibility = View.GONE
                        binding.tvBlocked.visibility = View.GONE
                    }
                }
            }
        }
        viewModel.fetchHistory(matchId)
        lifecycleScope.launch {
            val myUserId = getMyUserIdAsync()
            adapter = MessageAdapter(myUserId)
            binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMessages.adapter = adapter

            viewModel.messages.collectLatest { msgs ->
                adapter.setMessages(msgs)
                if (msgs.isNotEmpty()) binding.rvMessages.scrollToPosition(msgs.size - 1)
            }
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.tvUserName.text = matchedUserName ?: "Chat"

        binding.btnImage.setOnClickListener {
            pickImage.launch("image/*")
        }
        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE,
                )
                return@setOnClickListener
            }
            val photoFile = File(requireContext().cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            cameraImageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile,
            )
            takePictureLauncher.launch(cameraImageUri)
        }
        binding.btnSend.setOnClickListener {
            val content = binding.edtMessage.text.toString().trim()
            if (content.isNotEmpty()) {
                lifecycleScope.launch {
                    val confirmedUserId = getMyUserIdAsync()

                    val messageModel = MessageModel(
                        senderId = confirmedUserId,
                        matchId = matchId,
                        message = content,
                        imgChat = null,
                        audioPath = null,
                        duration = null,
                        timestamp = java.time.Instant.now().toString(),
                        delivered = true,
                    )

                    viewModel.addMessage(messageModel)
                    android.util.Log.d("ChatDetailFragment", "Added optimistic message: $content")
                    viewModel.sendMessageViaSocket(content)
                    binding.edtMessage.setText("")
                }
            }
        }

        binding.btnMore.setOnClickListener {
            lifecycleScope.launch {
                val myUserId = getMyUserIdAsync()
                val matchStatus = viewModel.matchStatus.value
                val blockerId = viewModel.blockerId.value

                val canUnblock = matchStatus == "blocked" && blockerId == myUserId
                val canBlock = matchStatus != "blocked" || (matchStatus == "blocked" && blockerId != myUserId)

                ChatMoreBottomSheet(
                    canBlock = canBlock,
                    canUnblock = canUnblock,
                    onUnmatch = if (!isAIConversation) {
                        { showUnmatchDialog() }
                    } else {
                        null
                    },
                    onReport = { },
                    onDeleteConversation = { showDeleteConversationDialog() },
                    onBlock = { showBlockUserDialog() },
                    onUnblock = { showUnblockUserDialog() },
                ).show(childFragmentManager, "moreSheet")
            }
        }

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
                            viewModel.sendAudioViaSocket(audioFilePath, duration)
                        }
                    } else {
                        Toast.makeText(context, "Bản ghi quá ngắn!", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setRecordingUI(isRecording: Boolean) {
        binding.tvRecording.visibility = if (isRecording) View.VISIBLE else View.GONE
        if (isRecording) {
            viewModel.hideAITypingIndicator()
        }
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
    private fun handlePickedImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                val file = uriToFile(uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val imageUrl = viewModel.uploadChatImage(body)
                if (!imageUrl.isNullOrEmpty()) {
                    val myUserId = getMyUserIdAsync()
                    viewModel.sendImageMessage(imageUrl, matchId, myUserId)
                } else {
                    Toast.makeText(context, "Upload ảnh lỗi!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Không chọn được file ảnh!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "img_${System.currentTimeMillis()}.jpg")
        val outputStream = file.outputStream()
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }
    private fun showPreviewImage(uri: Uri) {
        binding.photoPreviewLayout.visibility = View.VISIBLE
        Glide.with(this).load(uri).into(binding.previewImage)

        binding.btnSendPhoto.setOnClickListener {
            binding.photoPreviewLayout.visibility = View.GONE
            handlePickedImage(uri)
            cameraImageUri = null
        }

        binding.btnRetake.setOnClickListener {
            binding.photoPreviewLayout.visibility = View.GONE
            cameraImageUri = null
            binding.btnCamera.performClick()
        }
    }

    private fun showDeleteConversationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Conversation")
            .setMessage("Are you sure you want to delete all messages in this conversation? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAllMessages(matchId)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUnmatchDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Unmatch")
            .setMessage("Bạn chắc chắn muốn unmatch với người này? Bạn sẽ không thể chat lại nếu không match lại!")
            .setPositiveButton("Unmatch") { _, _ ->
                val targetUserId = arguments?.getString("targetUserId")
                if (targetUserId.isNullOrEmpty()) {
                    return@setPositiveButton
                }
                viewModel.unmatch(targetUserId) { success ->
                    if (success) {
                        viewModel.fetchMatchStatus(targetUserId)
                        Toast.makeText(
                            context,
                            "Unmatch thành công!",
                            Toast.LENGTH_SHORT,
                        ).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        Toast.makeText(context, "Unmatch thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showBlockUserDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Block")
            .setMessage("Sau khi chặn, bạn sẽ không thể gửi tin nhắn hoặc tương tác với người này.")
            .setPositiveButton("Block") { _, _ ->
                lifecycleScope.launch {
                    viewModel.blockUser(targetUserId) { success ->
                        if (success) {
                            Toast.makeText(context, "Đã chặn người này!", Toast.LENGTH_SHORT).show()
                            viewModel.fetchMatchStatus(targetUserId)
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        } else {
                            Toast.makeText(context, "Chặn thất bại!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private fun showUnblockUserDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Unblock")
            .setMessage("Bạn chắc chắn muốn bỏ chặn người này?")
            .setPositiveButton("Unblock") { _, _ ->
                viewModel.unblockUser(targetUserId) { success ->
                    if (success) {
                        Toast.makeText(context, "Đã bỏ chặn!", Toast.LENGTH_SHORT).show()
                        viewModel.fetchMatchStatus(targetUserId)
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        Toast.makeText(context, "Bỏ chặn thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun setupTypingIndicatorAnimation() {
        val lottieView = binding.aiTypingIndicator.findViewById<LottieAnimationView>(R.id.lottieTypingIndicator)
        lottieView?.let { view ->
            LottieCompositionFactory.fromRawRes(requireContext(), R.raw.typing_indicator)
                .addListener { composition ->
                    view.setComposition(composition)
                }
                .addFailureListener { throwable ->
                    android.util.Log.e("ChatDetailFragment", "Failed to load typing indicator animation", throwable)
                }
        }
    }

    private fun showAITypingIndicator() {
        binding.tvRecording.visibility = View.GONE
        binding.aiTypingIndicator.visibility = View.VISIBLE

        val lottieView = binding.aiTypingIndicator.findViewById<LottieAnimationView>(R.id.lottieTypingIndicator)
        lottieView?.playAnimation()
    }

    private fun hideAITypingIndicator() {
        binding.aiTypingIndicator.visibility = View.GONE

        val lottieView = binding.aiTypingIndicator.findViewById<LottieAnimationView>(R.id.lottieTypingIndicator)
        lottieView?.pauseAnimation()
    }
}
