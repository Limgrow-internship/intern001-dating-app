package com.intern001.dating.presentation.ui.profile.edit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intern001.dating.R

class ViewProfileInterestsAdapter(
    private val items: List<String>,
) : RecyclerView.Adapter<ViewProfileInterestsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInterest: TextView = itemView.findViewById(R.id.tv_interests)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_profile_interests, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvInterest.text = items[position]
    }

    override fun getItemCount() = items.size
}
