package com.intern001.dating.presentation.ui.chat

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intern001.dating.R
import com.intern001.dating.data.model.MessageModel

class MessageAdapter(
    private val myUserId: String,
    private val matchedUserName: String? = null,
    private val blockerId: String? = null,
    private val onMessageLongPress: (MessageModel) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages = listOf<MessageModel>()

    companion object {
        private const val VIEW_TYPE_LEFT_TEXT = 0
        private const val VIEW_TYPE_RIGHT_TEXT = 1
        private const val VIEW_TYPE_AUDIO_LEFT = 2
        private const val VIEW_TYPE_AUDIO_RIGHT = 3
    }
    private var mediaPlayer: MediaPlayer? = null
    private var isAdapterAudioPlaying = false
    private var currentAudioVH: AudioViewHolder? = null
    private var currentMsgPos: Int = -1

    fun setMessages(list: List<MessageModel>) {
        val filtered = list.filter { msg ->
            val hasContent = msg.message?.isNotBlank() == true ||
                !msg.imgChat.isNullOrBlank() ||
                !msg.audioPath.isNullOrBlank()
            if (!hasContent) return@filter false

            if (blockerId != null && myUserId == blockerId) {
                msg.delivered != false || msg.senderId == myUserId
            } else {
                true
            }
        }

        val diffCallback = MessageDiffCallback(messages, filtered)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        val reactionChanges = mutableListOf<Int>()
        for (i in filtered.indices) {
            val newMsg = filtered[i]
            val oldMsg = messages.firstOrNull {
                (it.id != null && newMsg.id != null && it.id == newMsg.id) ||
                    (it.clientMessageId != null && newMsg.clientMessageId != null && it.clientMessageId == newMsg.clientMessageId)
            }
            if (oldMsg != null && oldMsg.reaction != newMsg.reaction) {
                reactionChanges.add(i)
            }
        }

        messages = filtered
        diffResult.dispatchUpdatesTo(this)

        if (reactionChanges.isNotEmpty()) {
            reactionChanges.forEach { position ->
                notifyItemChanged(position)
            }
        }
    }

    private class MessageDiffCallback(
        private val oldList: List<MessageModel>,
        private val newList: List<MessageModel>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]

            if (old.id != null && new.id != null && old.id == new.id) {
                return true
            }

            if (old.clientMessageId != null &&
                new.clientMessageId != null &&
                old.clientMessageId == new.clientMessageId
            ) {
                return true
            }

            if ((old.id != null && old.id == new.id) ||
                (old.clientMessageId != null && old.clientMessageId == new.clientMessageId) ||
                (new.id != null && new.id == old.id) ||
                (new.clientMessageId != null && new.clientMessageId == old.clientMessageId)
            ) {
                return true
            }

            if (old.id == null &&
                old.clientMessageId == null &&
                new.id == null &&
                new.clientMessageId == null &&
                old.senderId == new.senderId &&
                old.message == new.message &&
                old.timestamp == new.timestamp
            ) {
                return true
            }

            return false
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]

            val isSame = old.message == new.message &&
                old.imgChat == new.imgChat &&
                old.audioPath == new.audioPath &&
                old.reaction == new.reaction &&
                old.delivered == new.delivered &&
                old.replyToMessageId == new.replyToMessageId &&
                old.replyPreview == new.replyPreview

            return isSame
        }
    }

    override fun getItemViewType(position: Int): Int {
        val msg = messages[position]
        val hasAudio = !msg.audioPath.isNullOrBlank()
        return when {
            hasAudio && msg.senderId == myUserId -> VIEW_TYPE_AUDIO_RIGHT
            hasAudio -> VIEW_TYPE_AUDIO_LEFT
            msg.senderId == myUserId -> VIEW_TYPE_RIGHT_TEXT
            else -> VIEW_TYPE_LEFT_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_RIGHT_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_right, parent, false)
                RightMessageVH(view)
            }
            VIEW_TYPE_LEFT_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_left, parent, false)
                LeftMessageVH(view)
            }
            VIEW_TYPE_AUDIO_RIGHT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.msg_voice_item_right, parent, false)
                AudioRightViewHolder(view)
            }
            VIEW_TYPE_AUDIO_LEFT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.msg_voice_item_left, parent, false)
                AudioLeftViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is RightMessageVH -> {
                holder.tvContent.text = msg.message ?: ""
                holder.tvContent.visibility = if (!msg.message.isNullOrBlank()) View.VISIBLE else View.GONE
                if (!msg.imgChat.isNullOrEmpty()) {
                    holder.imgChat.visibility = View.VISIBLE
                    Glide.with(holder.imgChat).load(msg.imgChat).into(holder.imgChat)
                } else {
                    holder.imgChat.visibility = View.GONE
                }
                bindReply(holder.replyContainer, holder.tvReplySender, holder.tvReplyText, msg)
                bindReaction(
                    msg = msg,
                    textReaction = holder.tvReaction,
                    imageReaction = holder.tvReactionImage,
                    hasText = !msg.message.isNullOrBlank(),
                    hasImage = !msg.imgChat.isNullOrBlank(),
                )
                holder.itemView.setOnLongClickListener {
                    onMessageLongPress.invoke(msg)
                    true
                }
            }
            is LeftMessageVH -> {
                holder.tvContent.text = msg.message ?: ""
                holder.tvContent.visibility = if (!msg.message.isNullOrBlank()) View.VISIBLE else View.GONE
                if (!msg.imgChat.isNullOrEmpty()) {
                    holder.imgChat.visibility = View.VISIBLE
                    Glide.with(holder.imgChat).load(msg.imgChat).into(holder.imgChat)
                } else {
                    holder.imgChat.visibility = View.GONE
                }
                bindReply(holder.replyContainer, holder.tvReplySender, holder.tvReplyText, msg)
                bindReaction(
                    msg = msg,
                    textReaction = holder.tvReaction,
                    imageReaction = holder.tvReactionImage,
                    hasText = !msg.message.isNullOrBlank(),
                    hasImage = !msg.imgChat.isNullOrBlank(),
                )
                holder.itemView.setOnLongClickListener {
                    onMessageLongPress.invoke(msg)
                    true
                }
            }
            is AudioRightViewHolder -> {
                bindAudioViewHolder(holder, msg, position)
                bindReaction(
                    msg = msg,
                    textReaction = holder.tvReaction,
                    imageReaction = null,
                    hasText = !msg.message.isNullOrBlank(),
                    hasImage = false,
                )
                holder.itemView.setOnLongClickListener {
                    onMessageLongPress.invoke(msg)
                    true
                }
            }
            is AudioLeftViewHolder -> {
                bindAudioViewHolder(holder, msg, position)
                bindReaction(
                    msg = msg,
                    textReaction = holder.tvReaction,
                    imageReaction = null,
                    hasText = !msg.message.isNullOrBlank(),
                    hasImage = false,
                )
                holder.itemView.setOnLongClickListener {
                    onMessageLongPress.invoke(msg)
                    true
                }
            }
        }
    }

    private fun bindReply(container: View, sender: TextView, text: TextView, msg: MessageModel) {
        val preview = when {
            !msg.replyPreview.isNullOrBlank() -> msg.replyPreview
            else -> {
                val ref = msg.replyToMessageId?.let { findReplySource(it) }
                ref?.let { buildPreviewFromMessage(it) }
            }
        }

        if (preview.isNullOrBlank()) {
            container.visibility = View.GONE
            return
        }
        container.visibility = View.VISIBLE

        val senderName = msg.replySenderName
            ?: msg.replySenderId?.let { if (it == myUserId) "Bạn" else (matchedUserName ?: "Họ") }
            ?: run {
                val replySource = msg.replyToMessageId?.let { findReplySource(it) }
                when {
                    replySource == null -> "Reply"
                    replySource.senderId == myUserId -> "Bạn"
                    else -> matchedUserName ?: "Họ"
                }
            }
        sender.text = senderName
        text.text = preview
    }

    private fun findReplySource(replyId: String): MessageModel? {
        return messages.firstOrNull {
            it.id == replyId || it.clientMessageId == replyId || it.timestamp == replyId
        }
    }

    private fun buildPreviewFromMessage(source: MessageModel): String {
        return when {
            source.message.isNotBlank() -> source.message
            !source.imgChat.isNullOrBlank() -> "[Hình ảnh]"
            !source.audioPath.isNullOrBlank() -> "[Ghi âm]"
            else -> "[Tin nhắn]"
        }
    }

    private fun bindReaction(
        msg: MessageModel,
        textReaction: TextView,
        imageReaction: TextView?,
        hasText: Boolean,
        hasImage: Boolean,
    ) {
        val emoji = msg.reaction

        if (emoji.isNullOrBlank()) {
            textReaction.visibility = View.GONE
            imageReaction?.visibility = View.GONE
            return
        }

        val showOnImage = hasImage && !hasText && imageReaction != null

        if (showOnImage) {
            imageReaction.text = emoji
            imageReaction.visibility = View.VISIBLE
            textReaction.visibility = View.GONE
        } else {
            textReaction.text = emoji
            textReaction.visibility = View.VISIBLE
            imageReaction?.visibility = View.GONE
        }
    }

    private fun bindAudioViewHolder(holder: AudioViewHolder, msg: MessageModel, position: Int) {
        holder.tvAudioDuration.text = formatDuration(msg.duration?.toInt() ?: 0)
        holder.btnTogglePlay.setImageResource(R.drawable.ic_audio_play)
        holder.imgWaveform.setImageResource(R.drawable.ic_waveform)
        holder.btnTogglePlay.setOnClickListener {
            if (isAdapterAudioPlaying && currentMsgPos == position) {
                stopAudio(holder)
            } else {
                playAudio(msg.audioPath, holder, position)
            }
        }
    }

    inner class LeftMessageVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val imgChat: ImageView = view.findViewById(R.id.imgChat)
        val replyContainer: View = view.findViewById(R.id.replyPreview)
        val tvReplySender: TextView = view.findViewById(R.id.tvReplySender)
        val tvReplyText: TextView = view.findViewById(R.id.tvReplyText)
        val tvReaction: TextView = view.findViewById(R.id.tvReaction)
        val tvReactionImage: TextView = view.findViewById(R.id.tvReactionImage)
    }
    inner class RightMessageVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val imgChat: ImageView = view.findViewById(R.id.imgChat)
        val replyContainer: View = view.findViewById(R.id.replyPreview)
        val tvReplySender: TextView = view.findViewById(R.id.tvReplySender)
        val tvReplyText: TextView = view.findViewById(R.id.tvReplyText)
        val tvReaction: TextView = view.findViewById(R.id.tvReaction)
        val tvReactionImage: TextView = view.findViewById(R.id.tvReactionImage)
    }
    open class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnTogglePlay: ImageButton = itemView.findViewById(R.id.btnTogglePlay)
        val imgWaveform: ImageView = itemView.findViewById(R.id.imgWaveform)
        val tvAudioDuration: TextView = itemView.findViewById(R.id.tvAudioDuration)
        val tvReaction: TextView = itemView.findViewById(R.id.tvReaction)
    }
    class AudioRightViewHolder(itemView: View) : AudioViewHolder(itemView)
    class AudioLeftViewHolder(itemView: View) : AudioViewHolder(itemView)

    private fun playAudio(audioPath: String?, holder: AudioViewHolder, pos: Int) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        currentAudioVH?.btnTogglePlay?.setImageResource(R.drawable.ic_audio_play)
        currentAudioVH?.imgWaveform?.setImageResource(R.drawable.ic_waveform)
        mediaPlayer = null
        currentAudioVH = holder
        currentMsgPos = pos

        if (audioPath == null) {
            Toast.makeText(holder.itemView.context, "Không có file audio!", Toast.LENGTH_SHORT).show()
            isAdapterAudioPlaying = false
            return
        }
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                prepare()
                start()
                isAdapterAudioPlaying = true
                holder.btnTogglePlay.setImageResource(R.drawable.ic_stop)
                setOnCompletionListener {
                    isAdapterAudioPlaying = false
                    holder.btnTogglePlay.setImageResource(R.drawable.ic_audio_play)
                    holder.imgWaveform.setImageResource(R.drawable.ic_waveform)
                    mediaPlayer?.release()
                    mediaPlayer = null
                    currentAudioVH = null
                    currentMsgPos = -1
                }
            }
        } catch (e: Exception) {
            Toast.makeText(holder.itemView.context, "Không phát được file audio!", Toast.LENGTH_SHORT).show()
            isAdapterAudioPlaying = false
            holder.btnTogglePlay.setImageResource(R.drawable.ic_audio_play)
        }
    }

    private fun stopAudio(holder: AudioViewHolder) {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        isAdapterAudioPlaying = false
        holder.btnTogglePlay.setImageResource(R.drawable.ic_audio_play)
        holder.imgWaveform.setImageResource(R.drawable.ic_waveform)
        currentAudioVH = null
        currentMsgPos = -1
    }

    private fun formatDuration(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return if (m > 0) "$m:${s.toString().padStart(2, '0')}" else "0:${s.toString().padStart(2, '0')}"
    }

    fun getAllMessages(): List<MessageModel> = messages
}
