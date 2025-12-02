package com.intern001.dating.presentation.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intern001.dating.R

class ChatMoreBottomSheet(
    val onUnmatch: () -> Unit,
    val onReport: () -> Unit,
    val onDeleteConversation: () -> Unit,
    val onBlock: () -> Unit,
) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_chat_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.tvUnmatch).setOnClickListener {
            dismiss()
            onUnmatch()
        }
        view.findViewById<TextView>(R.id.tvReport).setOnClickListener {
            dismiss()
            onReport()
        }
        view.findViewById<TextView>(R.id.tvDeleteConversation).setOnClickListener {
            dismiss()
            onDeleteConversation()
        }
        view.findViewById<TextView>(R.id.tvBlock).setOnClickListener {
            dismiss()
            onBlock()
        }
    }
}
