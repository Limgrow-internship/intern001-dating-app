package com.intern001.dating.presentation.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intern001.dating.R

class ChatMoreBottomSheet(
    val onUnmatch: (() -> Unit)? = null,
    val onReport: () -> Unit,
    val onDeleteConversation: () -> Unit,
    val onBlock: () -> Unit,
    val onUnblock: () -> Unit,
    val canBlock: Boolean,
    val canUnblock: Boolean,
) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_chat_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvUnmatch = view.findViewById<TextView>(R.id.tvUnmatch)
        if (onUnmatch != null) {
            tvUnmatch.setOnClickListener {
                dismiss()
                onUnmatch()
            }
        } else {
            // Hide unmatch option if null (e.g., for AI conversation)
            tvUnmatch.visibility = View.GONE
        }
        view.findViewById<TextView>(R.id.tvReport).setOnClickListener {
            dismiss()
            onReport()
        }
        view.findViewById<TextView>(R.id.tvDeleteConversation).setOnClickListener {
            dismiss()
            onDeleteConversation()
        }
        val tvBlock = view.findViewById<TextView>(R.id.tvBlock)
        val tvUnBlock = view.findViewById<TextView>(R.id.tvUnBlock)

        tvBlock.visibility = if (canBlock) View.VISIBLE else View.GONE
        tvUnBlock.visibility = if (canUnblock) View.VISIBLE else View.GONE

        tvBlock.setOnClickListener {
            dismiss()
            onBlock()
        }
        tvUnBlock.setOnClickListener {
            dismiss()
            onUnblock()
        }

        if (onUnmatch != null) {
            tvUnmatch.setOnClickListener {
                dismiss()
                onUnmatch()
            }
        } else {
            tvUnmatch.visibility = View.GONE
        }

        view.findViewById<TextView>(R.id.tvReport).setOnClickListener {
            dismiss()
            onReport()
        }
        view.findViewById<TextView>(R.id.tvDeleteConversation).setOnClickListener {
            dismiss()
            onDeleteConversation()
        }
    }
}
