package com.intern001.dating.presentation.ui.chat.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.intern001.dating.R

class ReportAdapter(
    private val items: List<ReportItem>,
    private val onClick: (ReportItem) -> Unit,
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    data class ReportItem(
        val key: String,
        val title: String,
    )

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val ivCheck: ImageView = view.findViewById(R.id.ivCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.tvTitle.text = item.title

        if (position == selectedPosition) {
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.orange_500))
            holder.ivCheck.visibility = View.VISIBLE
        } else {
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.black))
            holder.ivCheck.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val old = selectedPosition
            selectedPosition = holder.adapterPosition

            notifyItemChanged(old)
            notifyItemChanged(selectedPosition)

            onClick(item)
        }
    }

    override fun getItemCount() = items.size
}
