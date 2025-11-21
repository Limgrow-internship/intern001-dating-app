package com.intern001.dating.presentation.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.intern001.dating.R

class MatchesAdapter(private val matches: List<ChatListFragment.Match>) : RecyclerView.Adapter<MatchesAdapter.MatchViewHolder>() {

    inner class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imvAvatar: ImageView = itemView.findViewById(R.id.imvAvatar)
        val tvNameAge: TextView = itemView.findViewById(R.id.tvNameAge)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val item = matches[position]
        Glide.with(holder.itemView.context)
            .load(item.avatarRes)
            .transform(RoundedCorners(30))
            .into(holder.imvAvatar)
        holder.tvNameAge.text = "${item.name}, ${item.age}"
        holder.tvLocation.text = item.location
    }

    override fun getItemCount() = matches.size
}
