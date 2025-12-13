package com.intern001.dating.presentation.ui.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.intern001.dating.R
import com.intern001.dating.domain.model.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private val onNotificationClick: (Notification) -> Unit,
) : ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position), onNotificationClick)
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivNotificationIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvNotificationTime)

        fun bind(notification: Notification, onClick: (Notification) -> Unit) {
            val ctx = itemView.context
            // Set icon based on icon type
            val iconRes = when (notification.iconType) {
                Notification.NotificationIconType.HEART -> R.drawable.ic_heart_notification
                Notification.NotificationIconType.MATCH -> R.drawable.ic_match_notification
                Notification.NotificationIconType.SETTINGS -> R.drawable.ic_setting_notification
            }
            ivIcon.setImageResource(iconRes)

            tvTitle.text = notification.title
            tvMessage.text = notification.message
            tvTime.text = formatTimestamp(notification.timestamp)

            // Update appearance based on read status
            if (notification.isRead) {
                itemView.setBackgroundResource(R.drawable.bg_notification_item_read)
                val readColor = ContextCompat.getColor(ctx, R.color.notification_read_text)
                tvTitle.setTextColor(readColor)
                tvMessage.setTextColor(readColor)
                tvTime.setTextColor(readColor)
            } else {
                itemView.setBackgroundResource(R.drawable.bg_notification_item)
                val unreadColor = ContextCompat.getColor(ctx, R.color.notification_unread_text)
                tvTitle.setTextColor(unreadColor)
                tvMessage.setTextColor(unreadColor)
                tvTime.setTextColor(unreadColor)
            }

            itemView.setOnClickListener {
                onClick(notification)
            }
        }

        private fun formatTimestamp(date: Date): String {
            val now = Date()
            val diff = now.time - date.time

            // Less than 1 minute
            if (diff < 60 * 1000) {
                return "Just now"
            }

            // Less than 1 hour
            if (diff < 60 * 60 * 1000) {
                val minutes = (diff / (60 * 1000)).toInt()
                return "${minutes}m ago"
            }

            // Less than 24 hours
            if (diff < 24 * 60 * 60 * 1000) {
                val hours = (diff / (60 * 60 * 1000)).toInt()
                return "${hours}h ago"
            }

            // Today - show time
            val todayFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            if (todayDate.format(date) == todayDate.format(now)) {
                return todayFormat.format(date)
            }

            // Yesterday
            val yesterday = Date(now.time - 24 * 60 * 60 * 1000)
            if (todayDate.format(date) == todayDate.format(yesterday)) {
                return "Yesterday"
            }

            // Same year - show date without year
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            if (yearFormat.format(date) == yearFormat.format(now)) {
                return dateFormat.format(date)
            }

            // Different year - show full date
            return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
        }
    }

    companion object {
        object NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
            override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem == newItem
            }
        }
    }
}
