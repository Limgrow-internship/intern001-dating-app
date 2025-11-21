package com.intern001.dating.presentation.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intern001.dating.R
import com.intern001.dating.presentation.common.viewmodel.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatListFragment : BaseFragment() {

    data class Match(val name: String, val age: Int, val location: String, val avatarRes: Int)
    data class Conversation(
        val userName: String,
        val lastMessage: String,
        val timestamp: String,
        val avatarRes: Int,
        val isOnline: Boolean,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val matches = listOf(
//            Match("Minh Hang", 21, "DaNang", R.drawable.avt_match),
//            Match("Bich Phuong", 22, "HaNoi", R.drawable.avt_match),
//            Match("Ngoc", 20, "SaiGon", R.drawable.avt_match),
//                Match("Bich Phuong", 22, "HaNoi", R.drawable.avt_match),
//        Match("Ngoc", 20, "SaiGon", R.drawable.avt_match)
//        )
//
//        val conversations = listOf(
//            Conversation("Minh Hang", "Nice to meet you", "12:00", R.drawable.avt_match, true),
//            Conversation("Nha Y", "Coffee or tea person?", "13:00", R.drawable.avt_match, false),
//            Conversation("Ngoc", "Just seeing where things go — maybe...", "9:00", R.drawable.avt_match, true)
//        )

        // empty matches:
        val matches = emptyList<Match>()

        // empty conversations:
        val conversations = emptyList<Conversation>()

        val rvMatches = view.findViewById<RecyclerView>(R.id.rvMatches)
        rvMatches.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvMatches.adapter = MatchesAdapter(matches)
        rvMatches.visibility = if (matches.isNotEmpty()) View.VISIBLE else View.GONE
        view.findViewById<View>(R.id.noMatchesCard).visibility = if (matches.isEmpty()) View.VISIBLE else View.GONE

        val rvConversations = view.findViewById<RecyclerView>(R.id.rvConversations)
        rvConversations.layoutManager = LinearLayoutManager(context)
        rvConversations.adapter = ConversationAdapter(conversations)
        rvConversations.visibility = if (conversations.isNotEmpty()) View.VISIBLE else View.GONE
        view.findViewById<View>(R.id.noChatsLayout).visibility = if (conversations.isEmpty()) View.VISIBLE else View.GONE
    }
}
