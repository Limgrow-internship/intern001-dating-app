package com.intern001.dating.presentation.ui.chat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.intern001.dating.R
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.data.service.HeartOnMessagingService
import com.intern001.dating.databinding.FragmentChatListBinding
import com.intern001.dating.domain.model.MatchList
import com.intern001.dating.domain.model.UserProfileMatch
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.common.viewmodel.ChatListViewModel
import com.intern001.dating.presentation.util.AIConstants
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatListFragment : BaseFragment() {

    data class Conversation(
        val matchId: String,
        val userId: String,
        val avatarUrl: String?,
        val userName: String,
        val lastMessage: String?,
        val timestamp: String?,
        val isOnline: Boolean?,
        val targetUserId: String,
    )

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val vm: ChatListViewModel by activityViewModels()
    private val chatSharedViewModel: ChatSharedViewModel by activityViewModels()
    private lateinit var matchAdapter: MatchAdapter
    private lateinit var conversationAdapter: ConversationAdapter
    private val chatBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            if (action == HeartOnMessagingService.ACTION_CHAT_MESSAGE) {
                val matchId = intent.getStringExtra(HeartOnMessagingService.EXTRA_MATCH_ID)
                val lastMsg = intent.getStringExtra(HeartOnMessagingService.EXTRA_MESSAGE) ?: ""
                val senderId = intent.getStringExtra(HeartOnMessagingService.EXTRA_SENDER_ID) ?: ""
                val ts = intent.getStringExtra(HeartOnMessagingService.EXTRA_TIMESTAMP)
                    ?: java.time.Instant.now().toString()

                if (!matchId.isNullOrBlank()) {
                    vm.updateLastMessage(
                        matchId,
                        com.intern001.dating.domain.entity.LastMessageEntity(
                            message = lastMsg,
                            senderId = senderId,
                            timestamp = ts,
                        ),
                    )
                    vm.refreshMatches()
                } else {
                    vm.refreshMatches()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        matchAdapter = MatchAdapter { match ->
            val clickedUserId = match.matchedUser.userId
            findNavController().navigate(
                R.id.action_chatList_to_datingMode,
                bundleOf(
                    "targetListUserId" to clickedUserId,
                    "allowMatchedProfile" to true,
                ),
            )
        }

        binding.rvMatches.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.rvMatches.adapter = matchAdapter

        binding.rvConversations.layoutManager = LinearLayoutManager(requireContext())

        conversationAdapter = ConversationAdapter(listOf()) { conversation ->
            chatSharedViewModel.preloadMessages(conversation.matchId)

            viewLifecycleOwner.lifecycleScope.launch {
                kotlinx.coroutines.delay(100)

                findNavController().navigate(
                    R.id.action_chatList_to_chatDetail,
                    bundleOf(
                        "matchId" to conversation.matchId,
                        "matchedUserName" to conversation.userName,
                        "matchedUserAvatar" to (conversation.avatarUrl ?: ""),
                        "targetUserId" to conversation.targetUserId,
                    ),
                )
            }
        }

        binding.rvConversations.adapter = conversationAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.isLoading.combine(vm.matches) { isLoading, matches ->
                    isLoading to matches
                }.combine(vm.lastMessagesCache) { (isLoading, matches), lastMessagesCache ->
                    Triple(isLoading, matches, lastMessagesCache)
                }.collect { (isLoading, matches, lastMessagesCache) ->

                    if (isLoading) {
                        binding.matchPlaceholder.isVisible = true
                        binding.rvMatches.isVisible = false
                        binding.noMatchesCard?.isVisible = false
                        binding.rvConversations.isVisible = false
                        binding.noChatsLayout.isVisible = false
                        return@collect
                    }

                    val aiMatchFromBackend = matches.firstOrNull {
                        AIConstants.isAIUser(it.matchedUser.userId)
                    }

                    val filteredMatchesForMatchList = matches
                        .filterNot { AIConstants.isAIUser(it.matchedUser.userId) }
                        .filter { it.status.equals("active", ignoreCase = true) }

                    val aiMatch = if (aiMatchFromBackend != null) {
                        aiMatchFromBackend.copy(
                            matchedUser = aiMatchFromBackend.matchedUser.copy(
                                name = AIConstants.AI_FAKE_NAME,
                                avatarUrl = AIConstants.AI_FAKE_AVATAR_URL,
                                age = aiMatchFromBackend.matchedUser.age ?: AIConstants.AI_FAKE_AGE,
                                city = aiMatchFromBackend.matchedUser.city ?: AIConstants.AI_FAKE_CITY,
                            ),
                        )
                    } else {
                        MatchList(
                            matchId = AIConstants.AI_FAKE_MATCH_ID,
                            status = "active",
                            lastActivityAt = Instant.now().toString(),
                            matchedUser = UserProfileMatch(
                                userId = AIConstants.AI_ASSISTANT_USER_ID,
                                name = AIConstants.AI_FAKE_NAME,
                                avatarUrl = AIConstants.AI_FAKE_AVATAR_URL,
                                age = AIConstants.AI_FAKE_AGE,
                                city = AIConstants.AI_FAKE_CITY,
                            ),
                        )
                    }

                    val matchesForAdapter = listOf(aiMatch) + filteredMatchesForMatchList
                    android.util.Log.d("MatchAdapterTest", "Submit matches: ${matchesForAdapter.size}")
                    matchAdapter.submitList(matchesForAdapter)

                    val hasMatches = matchesForAdapter.isNotEmpty()
                    binding.rvMatches.isVisible = hasMatches
                    binding.matchPlaceholder.isVisible = !hasMatches
                    binding.noMatchesCard?.isVisible = !hasMatches

                    val matchesForConversation = matches.filterNot { AIConstants.isAIUser(it.matchedUser.userId) }
                    val allConversations = listOf(aiMatch) + matchesForConversation

                    val conversations = allConversations.map { match ->
                        val lastMsg = lastMessagesCache[match.matchId]
                        Conversation(
                            matchId = match.matchId,
                            userId = match.matchedUser.userId,
                            avatarUrl = if (AIConstants.isAIUser(match.matchedUser.userId)) AIConstants.AI_FAKE_AVATAR_URL else match.matchedUser.avatarUrl,
                            userName = if (AIConstants.isAIUser(match.matchedUser.userId)) AIConstants.AI_FAKE_NAME else match.matchedUser.name,
                            lastMessage = lastMsg?.message,
                            timestamp = lastMsg?.timestamp,
                            isOnline = null,
                            targetUserId = match.matchedUser.userId,
                        )
                    }
                    val sortedConversations = conversations.sortedWith(
                        compareBy<Conversation> { !AIConstants.isAIUser(it.userId) }
                            .thenByDescending { it.timestamp ?: "" },
                    )
                    conversationAdapter.setData(sortedConversations)
                    binding.rvConversations.isVisible = conversations.isNotEmpty()
                    binding.noChatsLayout.isVisible = conversations.isEmpty()

                    val matchesWithoutCache = allConversations.filter { !lastMessagesCache.containsKey(it.matchId) }
                    if (matchesWithoutCache.isNotEmpty()) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            matchesWithoutCache.forEach { match ->
                                if (vm.lastMessagesCache.value[match.matchId] == null) {
                                } else {
                                    vm.getLastMessage(match.matchId)
                                }
                            }
                        }
                    }

                    // Listen to shared message cache (socket/FCM) to update last message previews
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                            chatSharedViewModel.messagesCache.collectLatest { cache ->
                                cache.forEach { (matchId, messages) ->
                                    if (messages.isNotEmpty()) {
                                        val latest = messages.maxByOrNull { it.timestamp ?: "" } ?: return@forEach
                                        vm.updateLastMessage(
                                            matchId,
                                            com.intern001.dating.domain.entity.LastMessageEntity(
                                                message = latest.previewText(),
                                                senderId = latest.senderId ?: "",
                                                timestamp = latest.timestamp ?: "",
                                            ),
                                        )
                                    } else {
                                        vm.updateLastMessage(matchId, null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        vm.fetchMatches()
    }

    override fun onResume() {
        super.onResume()
        vm.refreshMatches()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(HeartOnMessagingService.ACTION_CHAT_MESSAGE)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.RECEIVER_NOT_EXPORTED
        } else {
            0
        }
        ContextCompat.registerReceiver(
            requireContext(),
            chatBroadcastReceiver,
            filter,
            flags,
        )
    }

    override fun onStop() {
        super.onStop()
        try {
            requireContext().unregisterReceiver(chatBroadcastReceiver)
        } catch (_: Exception) {
        }
    }

    private fun MessageModel.previewText(): String {
        return when {
            message.isNotBlank() -> message
            !imgChat.isNullOrBlank() -> "[image]"
            !audioPath.isNullOrBlank() -> "[audio]"
            else -> ""
        }
    }
}
