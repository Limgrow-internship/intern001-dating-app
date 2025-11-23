package com.intern001.dating.presentation.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intern001.dating.R

class ConversationAdapter(private val conversations: List<ChatListFragment.Conversation>) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

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
            .load(c.avatarRes)
            .circleCrop()
            .into(holder.imvAvatar)
        holder.tvUserName.text = c.userName
        holder.tvLastMessage.text = c.lastMessage
        holder.tvTimestamp.text = c.timestamp
        holder.onlineDot.visibility = if (c.isOnline) View.VISIBLE else View.INVISIBLE
    }

    override fun getItemCount() = conversations.size
}
