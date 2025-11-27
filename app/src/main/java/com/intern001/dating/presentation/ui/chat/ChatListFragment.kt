package com.intern001.dating.presentation.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.intern001.dating.databinding.FragmentChatListBinding
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import com.intern001.dating.presentation.common.viewmodel.ChatListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatListFragment : BaseFragment() {

    data class Conversation(
        val avatarUrl: String?,
        val userName: String,
        val lastMessage: String?,
        val timestamp: String?,
        val isOnline: Boolean?,
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

        // MatchList
        binding.rvMatches.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        matchAdapter = MatchAdapter()
        binding.rvMatches.adapter = matchAdapter

        // Conversation list
        binding.rvConversations.layoutManager = LinearLayoutManager(requireContext())
        conversationAdapter = ConversationAdapter(listOf())
        binding.rvConversations.adapter = conversationAdapter

        vm.matches.observe(viewLifecycleOwner) { matches ->

            val hasMatches = !matches.isNullOrEmpty()
            binding.matchPlaceholder.isVisible = !hasMatches
            binding.rvMatches.isVisible = hasMatches

            matchAdapter.submitList(matches)
            binding.noMatchesCard?.isVisible = matches.isNullOrEmpty()

            val conversations = matches?.map { match ->
                Conversation(
                    avatarUrl = match.matchedUser.avatarUrl,
                    userName = match.matchedUser.name,
                    lastMessage = null,
                    timestamp = null,
                    isOnline = null,
                )
            } ?: emptyList()
            conversationAdapter = ConversationAdapter(conversations)
            binding.rvConversations.adapter = conversationAdapter

            val hasChat = conversations.isNotEmpty()
            binding.rvConversations.isVisible = hasChat
            binding.noChatsLayout.isVisible = !hasChat
        }

        val token = "YourTokenHere"
        vm.fetchMatches(token)
    }
}
