package com.intern001.dating.presentation.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.heartondatingapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }
}
