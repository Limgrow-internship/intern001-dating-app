package com.intern001.dating.presentation.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intern001.dating.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatListFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }
}
