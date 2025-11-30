// ConversationAdapter.kt
package com.intern001.dating.presentation.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intern001.dating.R
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ConversationAdapter(
    private var conversations: List<ChatListFragment.Conversation>,
    private val onClick: (ChatListFragment.Conversation) -> Unit,
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    fun setData(newData: List<ChatListFragment.Conversation>) {
        conversations = newData
        notifyDataSetChanged()
    }

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imvAvatar: ImageView = itemView.findViewById(R.id.avatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val onlineDot: View = itemView.findViewById(R.id.onlineDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val c = conversations[position]
        Glide.with(holder.itemView.context)
            .load(c.avatarUrl)
            .placeholder(R.drawable.bg_avatar_round)
            .circleCrop()
            .into(holder.imvAvatar)
        holder.tvUserName.text = c.userName
        holder.tvLastMessage.text = c.lastMessage ?: "Say hi match!"
        holder.tvTimestamp.text = c.timestamp?.let { formatChatTimestamp(it) } ?: ""
        holder.onlineDot.visibility = if (c.isOnline == true) View.VISIBLE else View.INVISIBLE

        holder.itemView.setOnClickListener { onClick(c) }
    }

    override fun getItemCount() = conversations.size

    private fun formatChatTimestamp(isoString: String): String {
        return try {
            val instant = Instant.parse(isoString)
            val msgDateTime = instant.atZone(ZoneId.systemDefault())
            val now = LocalDate.now(msgDateTime.zone)
            if (msgDateTime.toLocalDate().isEqual(now)) {
                msgDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            } else {
                when (val dayOfWeek = msgDateTime.dayOfWeek) {
                    DayOfWeek.MONDAY -> "Mon"
                    DayOfWeek.TUESDAY -> "Tue"
                    DayOfWeek.WEDNESDAY -> "Wed"
                    DayOfWeek.THURSDAY -> "Thu"
                    DayOfWeek.FRIDAY -> "Fri"
                    DayOfWeek.SATURDAY -> "Sat"
                    DayOfWeek.SUNDAY -> "Sun"
                    else -> msgDateTime.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))
                }
            }
        } catch (e: Exception) {
            ""
        }
    }
}
