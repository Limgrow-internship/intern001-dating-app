// ChatListFragment.kt
package com.intern001.dating.presentation.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.intern001.dating.R
import com.intern001.dating.databinding.FragmentChatListBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.common.viewmodel.ChatListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    private val vm: ChatListViewModel by viewModels()
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

        binding.rvMatches.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        matchAdapter =
            MatchAdapter { match ->
                findNavController().navigate(
                    R.id.action_chatList_to_datingMode,
                    bundleOf(
                        "targetUserId" to match.matchedUser.userId,
                        "allowMatchedProfile" to true,
                    ),
                )
            }
        binding.rvMatches.adapter = matchAdapter

        binding.rvConversations.layoutManager = LinearLayoutManager(requireContext())

        conversationAdapter = ConversationAdapter(listOf()) { conversation ->
            findNavController().navigate(
                R.id.action_chatList_to_chatDetail,
                bundleOf(
                    "matchId" to conversation.matchId,
                    "matchedUserName" to conversation.userName,
                    "matchedUserAvatar" to conversation.avatarUrl,
                    "targetUserId" to conversation.targetUserId,
                ),
            )
        }

        binding.rvConversations.adapter = conversationAdapter

        // --- OBSERVE DATA
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.isLoading.combine(vm.matches) { isLoading, matches ->
                    isLoading to matches
                }.collect { (isLoading, matches) ->

                    if (isLoading) {
                        binding.matchPlaceholder.isVisible = true
                        binding.rvMatches.isVisible = false
                        binding.noMatchesCard?.isVisible = false
                        binding.rvConversations.isVisible = false
                        binding.noChatsLayout.isVisible = false
                        return@collect
                    }

                    val hasMatches = matches.isNotEmpty()

                    // Show / hide UI components
                    binding.rvMatches.isVisible = hasMatches
                    binding.matchPlaceholder.isVisible = false
                    binding.noMatchesCard?.isVisible = !hasMatches

                    matchAdapter.submitList(matches)

                    // --- Build conversations
                    val conversations = matches.map { match ->
                        async {
                            val lastMsg = vm.getLastMessage(match.matchId)

                            Conversation(
                                matchId = match.matchId,
                                userId = match.matchedUser.userId, // ✔ LẤY userId ở đây
                                avatarUrl = match.matchedUser.avatarUrl,
                                userName = match.matchedUser.name,
                                lastMessage = lastMsg?.message,
                                timestamp = lastMsg?.timestamp,
                                isOnline = null,
                                targetUserId = match.matchedUser.userId,
                            )
                        }
                    }.awaitAll()

                    conversationAdapter.setData(conversations)

                    binding.rvConversations.isVisible = conversations.isNotEmpty()
                    binding.noChatsLayout.isVisible = conversations.isEmpty()
                }
            }
        }

        // Fetch data
        vm.fetchMatches("YourTokenHere")
    }
}
