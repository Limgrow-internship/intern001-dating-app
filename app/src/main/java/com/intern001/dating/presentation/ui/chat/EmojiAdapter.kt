package com.intern001.dating.presentation.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmojiAdapter(
    private val emojis: List<String>,
    private val onEmojiClick: (String) -> Unit,
) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

    inner class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiText: TextView = itemView.findViewById(android.R.id.text1)

        init {
            itemView.setOnClickListener {
                val emoji = emojis[adapterPosition]
                onEmojiClick(emoji)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        (view as TextView).textSize = 20f
        view.textAlignment = View.TEXT_ALIGNMENT_CENTER
        return EmojiViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.emojiText.text = emojis[position]
    }

    override fun getItemCount(): Int = emojis.size
}
