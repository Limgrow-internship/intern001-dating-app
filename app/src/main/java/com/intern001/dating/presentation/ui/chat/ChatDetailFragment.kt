package com.intern001.dating.presentation.ui.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.bumptech.glide.Glide
import com.intern001.dating.R
import com.intern001.dating.data.local.prefs.TokenManager
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.databinding.FragmentChatScreenBinding
import com.intern001.dating.domain.entity.LastMessageEntity
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.common.viewmodel.ChatListViewModel
import com.intern001.dating.presentation.util.AIConstants
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@AndroidEntryPoint
class ChatDetailFragment : BaseFragment() {
    private val viewModel: ChatViewModel by viewModels()
    private val chatSharedViewModel: ChatSharedViewModel by activityViewModels()
    private val chatListViewModel: ChatListViewModel by activityViewModels()
    private var matchId: String = ""
    private var matchedUserName: String? = null
    private lateinit var adapter: MessageAdapter

    private var _binding: FragmentChatScreenBinding? = null
    private val binding get() = _binding!!
    private val tokenManager by lazy {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        TokenManager(prefs)
    }

    private var targetUserId: String = ""

    private val CAMERA_REQUEST_CODE = 124

    private val emojiList = listOf(
        "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊", "😋", "😎", "😍", "😘", "😗", "😙", "😚", "☺️", "🙂", "🤗", "🤩", "🤔", "🤨",
        "😐", "😑", "😶", "🙄", "😏", "😣", "😥", "😮", "🤐", "😯", "😪", "😫", "😴", "😌", "😛", "😜", "😝", "🤤", "😒", "😓", "😔", "😕", "🙃",
        "🤑", "😲", "☹️", "🙁", "😖", "😞", "😟", "😤", "😢", "😭", "😦", "😧", "😨", "😩", "🤯", "😬", "😰", "😱", "😳", "🤪", "😵", "😡",
        "😠", "🤬", "😷", "🤒", "🤕", "🤢", "🤮", "🤧", "😇", "🥳", "🥰", "🤠", "🤡", "🥺",
    )

    private val defaultSuggestions = listOf(
        "Wave to %s",
        "Hi! Nice to meet you",
        "How’s your day going?",
        "Want to grab a coffee?",
    )

    private var recorder: MediaRecorder? = null
    private var audioFilePath: String = ""
    private var isRecording = false
    private var recordStartTime: Long = 0
    private var pendingReply: MessageModel? = null
    private var lastMessageCount: Int = 0
    private var lastMessageTimestamp: String = ""

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
        return tokenManager.getUserId() ?: ""
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
        val baseInputPadding = binding.messageInputLayout.paddingBottom
        val baseListPadding = binding.rvMessages.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val inputBottom = if (imeBottom > 0) imeBottom else sysBottom

            binding.messageInputLayout.updatePadding(bottom = baseInputPadding + inputBottom)
            binding.rvMessages.updatePadding(bottom = baseListPadding + sysBottom)

            insets
        }
        binding.root.setOnClickListener {
            hideKeyboardAndEmoji()
        }
        binding.rvMessages.setOnTouchListener { _, _ ->
            hideKeyboardAndEmoji()
            false
        }
        arguments?.let {
            matchId = it.getString("matchId", "")
            matchedUserName = it.getString("matchedUserName")
            targetUserId = it.getString("targetUserId", "")
        }

        isAIConversation = AIConstants.isAIUser(targetUserId)
        binding.btnMore.visibility = if (isAIConversation) View.GONE else View.VISIBLE

        if (isAIConversation && matchedUserName.isNullOrBlank()) {
            matchedUserName = AIConstants.AI_FAKE_NAME
        }

        setupTypingIndicatorAnimation()
        setupSuggestionChips()

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
        lifecycleScope.launch {
            val myUserId = getMyUserIdAsync()
            adapter = MessageAdapter(
                myUserId = myUserId,
                matchedUserName = matchedUserName,
                blockerId = null,
                onMessageLongPress = { msg ->
                    showMessageActionSheet(msg)
                },
            )
            binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMessages.adapter = adapter

            val preloadedMessages = chatSharedViewModel.getCachedMessages(matchId)
            val isPreloading = chatSharedViewModel.preloadingMatchIds.value.contains(matchId)

            if (preloadedMessages.isNotEmpty()) {
                android.util.Log.d("ChatDetailFragment", "Using preloaded messages from memory: ${preloadedMessages.size}")
                viewModel.updateMessagesFromCache(preloadedMessages)
            } else {
                try {
                    val localMessages = chatSharedViewModel.getCachedMessagesAsync(matchId)
                    if (localMessages.isNotEmpty()) {
                        android.util.Log.d("ChatDetailFragment", "Using messages from local DB: ${localMessages.size}")
                        viewModel.updateMessagesFromCache(localMessages)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChatDetailFragment", "Failed to load from local DB", e)
                }

                if (isPreloading) {
                    android.util.Log.d("ChatDetailFragment", "Waiting for preload to complete...")
                    kotlinx.coroutines.delay(200)

                    val cachedAfterWait = chatSharedViewModel.getCachedMessages(matchId)
                    if (cachedAfterWait.isNotEmpty()) {
                        android.util.Log.d("ChatDetailFragment", "Using preloaded messages after wait: ${cachedAfterWait.size}")
                        viewModel.updateMessagesFromCache(cachedAfterWait)
                    } else {
                        android.util.Log.d("ChatDetailFragment", "Preload taking too long, fetching directly...")
                        viewModel.fetchHistory(matchId)
                    }
                } else {
                    android.util.Log.d("ChatDetailFragment", "No cache, fetching from server...")
                    viewModel.fetchHistory(matchId)
                }
            }

            viewModel.messages.collectLatest { msgs ->
                val previousCount = lastMessageCount
                val previousTimestamp = lastMessageTimestamp

                adapter.setMessages(msgs)

                if (msgs.isNotEmpty()) {
                    val latestMsg = msgs.maxByOrNull { it.timestamp ?: "" }
                    val latestTimestamp = latestMsg?.timestamp ?: ""

                    val sizeIncreased = msgs.size > previousCount
                    val timestampChanged = latestTimestamp != previousTimestamp && latestTimestamp.isNotBlank()
                    val isNearBottom = isNearBottom()

                    if (sizeIncreased) {
                        binding.rvMessages.post {
                            binding.rvMessages.smoothScrollToPosition(msgs.size - 1)
                        }
                    } else if (timestampChanged && isNearBottom) {
                        binding.rvMessages.post {
                            binding.rvMessages.smoothScrollToPosition(msgs.size - 1)
                        }
                    }

                    lastMessageTimestamp = latestTimestamp
                }

                lastMessageCount = msgs.size
                chatSharedViewModel.updateMessages(matchId, msgs)
                toggleSuggestionVisibility(msgs.isEmpty())

                val latest = msgs.maxByOrNull { it.timestamp ?: "" }
                latest?.let { msg ->
                    val preview = when {
                        msg.message.isNotBlank() -> msg.message
                        !msg.imgChat.isNullOrBlank() -> "[image]"
                        !msg.audioPath.isNullOrBlank() -> "[audio]"
                        else -> ""
                    }
                    val ts = msg.timestamp ?: ""
                    chatListViewModel.updateLastMessage(
                        matchId,
                        LastMessageEntity(
                            message = preview,
                            senderId = msg.senderId,
                            timestamp = ts,
                        ),
                    )
                }
            }
        }

        lifecycleScope.launch {
            chatSharedViewModel.messagesCache.collectLatest { cache ->
                val cachedMessages = cache[matchId]
                if (cachedMessages != null && cachedMessages.isNotEmpty()) {
                    val currentMessages = viewModel.messages.value
                    if (currentMessages.isEmpty() || currentMessages.size < cachedMessages.size) {
                        if (::adapter.isInitialized) {
                            android.util.Log.d("ChatDetailFragment", "Cache updated, updating messages: ${cachedMessages.size}")
                            viewModel.updateMessagesFromCache(cachedMessages)
                        }
                    }
                }
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
            sendTextMessage(binding.edtMessage.text.toString())
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
                    onReport = {
                        if (isAIConversation) {
                            Toast.makeText(context, "Không thể báo cáo AI", Toast.LENGTH_SHORT).show()
                            return@ChatMoreBottomSheet
                        }
                        findNavController().navigate(
                            R.id.action_chatDetail_to_reportFragment,
                            Bundle().apply {
                                putString("targetUserId", targetUserId)
                            },
                        )
                    },
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
                            val clientId = viewModel.newClientMessageId()
                            viewModel.sendAudioViaSocket(audioFilePath, duration, clientId)
                        }
                    } else {
                        Toast.makeText(context, "Bản ghi quá ngắn!", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        binding.btnCloseReply.setOnClickListener {
            clearReplyBar()
        }
    }

    private fun setupSuggestionChips() {
        val displayName = matchedUserName?.takeIf { it.isNotBlank() } ?: "bạn"
        val suggestions = defaultSuggestions.map { text ->
            if (text.contains("%s")) text.format(displayName) else text
        }

        binding.suggestionChipGroup.removeAllViews()
        suggestions.forEach { text ->
            binding.suggestionChipGroup.addView(buildChip(text))
        }
        toggleSuggestionVisibility(viewModel.messages.value.isEmpty())
    }

    private fun buildChip(text: String): TextView {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            rightMargin = 8.dp
        }
        return TextView(requireContext()).apply {
            layoutParams = params
            this.text = text
            setTextColor(Color.parseColor("#333333"))
            textSize = 14f
            setPadding(16.dp, 10.dp, 16.dp, 10.dp)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chat_suggestion_chip)
            setOnClickListener {
                sendTextMessage(text)
            }
        }
    }

    private fun toggleSuggestionVisibility(show: Boolean) {
        binding.suggestionContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).roundToInt()

    private fun showMessageActionSheet(message: MessageModel) {
        val isMine = message.senderId == viewModel.currentUserId
        MessageActionBottomSheet(
            message = message,
            isMine = isMine,
            onReply = { msg ->
                pendingReply = msg
                val snippet = buildReplyPreview(msg)
                binding.tvReplyText.text = snippet
                binding.tvReplyName.text = if (msg.senderId == viewModel.currentUserId) "Bạn" else (matchedUserName ?: "Họ")
                binding.replyBar.visibility = View.VISIBLE
                binding.edtMessage.requestFocus()
            },
            onDeleteForMe = {
                Toast.makeText(context, "Đang phát triển: Xoá cho bạn", Toast.LENGTH_SHORT).show()
            },
            onUnsend = {
                Toast.makeText(context, "Đang phát triển: Thu hồi tin nhắn", Toast.LENGTH_SHORT).show()
            },
            onReact = { targetMsg, reaction ->
                val targetId = targetMsg.id ?: targetMsg.clientMessageId
                android.util.Log.d("ChatDetailFragment", "Applying reaction $reaction to message id=$targetId, clientId=${targetMsg.clientMessageId}")
                viewModel.applyReaction(
                    targetId = targetId,
                    reaction = reaction,
                )
            },
        ).show(childFragmentManager, "messageActions")
    }

    private fun sendTextMessage(content: String) {
        val message = content.trim()
        if (message.isEmpty()) return

        lifecycleScope.launch {
            val confirmedUserId = getMyUserIdAsync()
            val clientId = viewModel.newClientMessageId()
            val replyMsg = pendingReply

            val messageModel = MessageModel(
                id = null,
                clientMessageId = clientId,
                senderId = confirmedUserId,
                matchId = matchId,
                message = message,
                imgChat = null,
                audioPath = null,
                duration = null,
                timestamp = java.time.Instant.now().toString(),
                delivered = true,
                replyToMessageId = replyMsg?.id,
                replyToClientMessageId = replyMsg?.clientMessageId,
                replyToTimestamp = replyMsg?.timestamp,
                replyPreview = replyMsg?.let { buildReplyPreview(it) },
                replySenderId = replyMsg?.senderId,
                replySenderName = replyMsg?.let { if (it.senderId == confirmedUserId) "Bạn" else (matchedUserName ?: "Họ") },
            )

            viewModel.addMessage(messageModel)
            android.util.Log.d("ChatDetailFragment", "Added optimistic message: $message")
            viewModel.sendMessageViaSocket(
                text = message,
                clientMessageId = clientId,
                replyToMessageId = messageModel.replyToMessageId,
                replyToClientMessageId = messageModel.replyToClientMessageId,
                replyToTimestamp = messageModel.replyToTimestamp,
                replyPreview = messageModel.replyPreview,
                replySenderId = messageModel.replySenderId,
                replySenderName = messageModel.replySenderName,
            )
            binding.edtMessage.setText("")
            clearReplyBar()
        }
    }

    private fun buildReplyPreview(msg: MessageModel): String {
        return when {
            msg.message.isNotBlank() -> msg.message
            !msg.imgChat.isNullOrBlank() -> "[Hình ảnh]"
            !msg.audioPath.isNullOrBlank() -> "[Ghi âm]"
            else -> "[Tin nhắn]"
        }
    }

    private fun clearReplyBar() {
        pendingReply = null
        binding.replyBar.visibility = View.GONE
        binding.tvReplyText.text = ""
        binding.tvReplyName.text = ""
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
                viewModel.clearMessagesForMyself(matchId) {
                    chatListViewModel.updateLastMessage(matchId, null)
                    chatListViewModel.refreshMatches()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
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

    private fun hideKeyboardAndEmoji() {
        binding.emojiContainer.visibility = View.GONE
        binding.edtMessage.clearFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.edtMessage.windowToken, 0)
    }

    private fun isNearBottom(): Boolean {
        val layoutManager = binding.rvMessages.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager
        if (layoutManager == null || adapter.itemCount == 0) return true

        val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
        val totalItems = adapter.itemCount

        // Consider "near bottom" if within last 3 items or if last item is visible
        return lastVisiblePosition >= totalItems - 3 || lastVisiblePosition == totalItems - 1
    }
}
