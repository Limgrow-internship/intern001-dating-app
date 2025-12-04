package com.intern001.dating.presentation.ui.chat

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intern001.dating.R
import com.intern001.dating.data.model.MessageModel

class MessageAdapter(private val myUserId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
        messages = list.filter {
            it.message?.isNotBlank() == true ||
                !it.imgChat.isNullOrBlank() ||
                !it.audioPath.isNullOrBlank()
        }
        notifyDataSetChanged()
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
            }
            is AudioRightViewHolder -> {
                bindAudioViewHolder(holder, msg, position)
            }
            is AudioLeftViewHolder -> {
                bindAudioViewHolder(holder, msg, position)
            }
        }
    }

    // Shared logic for both audio left/right
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

    // -- ViewHolders --
    inner class LeftMessageVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val imgChat: ImageView = view.findViewById(R.id.imgChat)
    }
    inner class RightMessageVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val imgChat: ImageView = view.findViewById(R.id.imgChat)
    }
    open class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnTogglePlay: ImageButton = itemView.findViewById(R.id.btnTogglePlay)
        val imgWaveform: ImageView = itemView.findViewById(R.id.imgWaveform)
        val tvAudioDuration: TextView = itemView.findViewById(R.id.tvAudioDuration)
    }
    class AudioRightViewHolder(itemView: View) : AudioViewHolder(itemView)
    class AudioLeftViewHolder(itemView: View) : AudioViewHolder(itemView)

    // -- Audio control --
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
                holder.btnTogglePlay.setImageResource(R.drawable.ic_stop) // pause icon
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
