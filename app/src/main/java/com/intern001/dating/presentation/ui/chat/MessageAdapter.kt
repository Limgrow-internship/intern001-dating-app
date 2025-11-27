package com.intern001.dating.presentation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intern001.dating.R
import com.intern001.dating.data.model.MessageModel

class MessageAdapter(private val myUserId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var messages = listOf<MessageModel>()

    fun setMessages(list: List<MessageModel>) {
        messages = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == myUserId) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_right, parent, false)
            RightMessageVH(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_left, parent, false)
            LeftMessageVH(view)
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        if (holder is RightMessageVH) {
            holder.tvContent.text = msg.message
        } else if (holder is LeftMessageVH) {
            holder.tvContent.text = msg.message
        }
    }

    inner class LeftMessageVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvContent: TextView = view.findViewById(R.id.tvContent)
    }
    inner class RightMessageVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvContent: TextView = view.findViewById(R.id.tvContent)
    }
}
