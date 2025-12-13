package com.intern001.dating.presentation.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intern001.dating.R
import com.intern001.dating.data.model.MessageModel

class MessageActionBottomSheet(
    private val message: MessageModel,
    private val isMine: Boolean,
    private val onReply: (MessageModel) -> Unit,
    private val onDeleteForMe: (MessageModel) -> Unit,
    private val onUnsend: (MessageModel) -> Unit,
    private val onReact: (MessageModel, String) -> Unit,
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.dialog_message_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reactions = listOf(
            R.id.tvReactHeart to "❤️",
            R.id.tvReactLike to "👍",
            R.id.tvReactLaugh to "😂",
            R.id.tvReactWow to "😮",
            R.id.tvReactSad to "😢",
            R.id.tvReactAngry to "😡",
        )
        reactions.forEach { (id, reaction) ->
            view.findViewById<TextView>(id)?.setOnClickListener {
                dismiss()
                onReact(message, reaction)
            }
        }

        view.findViewById<TextView>(R.id.tvReply)?.setOnClickListener {
            dismiss()
            onReply(message)
        }

        view.findViewById<TextView>(R.id.tvDeleteForMe)?.setOnClickListener {
            dismiss()
            onDeleteForMe(message)
        }

        view.findViewById<TextView>(R.id.tvUnsend)?.apply {
            visibility = if (isMine) View.VISIBLE else View.GONE
            setOnClickListener {
                dismiss()
                onUnsend(message)
            }
        }
    }
}
