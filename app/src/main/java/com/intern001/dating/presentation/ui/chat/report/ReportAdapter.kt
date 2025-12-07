package com.intern001.dating.presentation.ui.chat.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.intern001.dating.R
class ReportAdapter(
    private val items: List<ReportItem>,
    private val onClick: (ReportItem) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    data class ReportItem(
        val key: String,
        val title: String
    )

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context, item.title, Toast.LENGTH_SHORT).show()
            onClick(item)
        }

    }

    override fun getItemCount() = items.size
}

