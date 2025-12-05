package com.intern001.dating.presentation.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentChatListBinding
import com.intern001.dating.domain.model.MatchList
import com.intern001.dating.domain.model.UserProfileMatch
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.common.viewmodel.ChatListViewModel
import com.intern001.dating.presentation.ui.chat.AIConstants
import com.intern001.dating.presentation.ui.chat.MatchAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
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

    // Activity-scoped ViewModel để có thể preload từ MainActivity
    private val vm: ChatListViewModel by activityViewModels()

    // Activity-scoped ViewModel để preload messages
    private val chatSharedViewModel: ChatSharedViewModel by activityViewModels()
    private lateinit var matchAdapter: MatchAdapter
    private lateinit var conversationAdapter: ConversationAdapter

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
            // Preload messages trước khi navigate
            chatSharedViewModel.preloadMessages(conversation.matchId)

            // Đợi một chút để preload có thời gian bắt đầu (non-blocking)
            viewLifecycleOwner.lifecycleScope.launch {
                // Đợi 100ms để preload có cơ hội bắt đầu
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

                    val filteredMatches = matches.filterNot {
                        AIConstants.isAIUser(it.matchedUser.userId)
                    }

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

                    val allMatches = listOf(aiMatch) + filteredMatches
                    val hasMatches = allMatches.isNotEmpty()

                    binding.rvMatches.isVisible = hasMatches
                    binding.matchPlaceholder.isVisible = false
                    binding.noMatchesCard?.isVisible = !hasMatches

                    matchAdapter.submitList(allMatches)

                    // Tạo conversations với last messages từ cache
                    val conversations = allMatches.map { match ->
                        val lastMsg = lastMessagesCache[match.matchId]

                        Conversation(
                            matchId = match.matchId,
                            userId = match.matchedUser.userId,
                            avatarUrl = if (AIConstants.isAIUser(match.matchedUser.userId)) {
                                AIConstants.AI_FAKE_AVATAR_URL
                            } else {
                                match.matchedUser.avatarUrl
                            },
                            userName = if (AIConstants.isAIUser(match.matchedUser.userId)) {
                                AIConstants.AI_FAKE_NAME
                            } else {
                                match.matchedUser.name
                            },
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

                    // Nếu có matches chưa có last message trong cache, fetch async (không block UI)
                    // Khi fetch xong, lastMessagesCache sẽ update và flow sẽ tự động trigger lại
                    val matchesWithoutCache = allMatches.filter { !lastMessagesCache.containsKey(it.matchId) }
                    if (matchesWithoutCache.isNotEmpty()) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            matchesWithoutCache.forEach { match ->
                                vm.getLastMessage(match.matchId) // Sẽ tự động cache khi fetch xong
                            }
                        }
                    }
                }
            }
        }

        // Nếu chưa có data, fetch ngay
        if (!vm.hasData()) {
            vm.fetchMatches()
        }
    }
}
