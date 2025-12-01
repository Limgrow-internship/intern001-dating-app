package com.intern001.dating.presentation.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.intern001.dating.databinding.ItemLikedYouBinding
import com.intern001.dating.domain.model.LikedYouUser

class LikedYouAdapter(
    private var items: List<LikedYouUser> = emptyList(),
    private val onItemClick: (LikedYouUser) -> Unit
) : RecyclerView.Adapter<LikedYouAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLikedYouBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LikedYouUser) = with(binding) {

            txtNameAge.text = item.displayName ?: "Chưa cập nhật"
            txtLocation.text = item.city ?: "Chưa cập nhật"

            val hdUrl = "${item.avatar}?q_auto:best&f_auto&c_fill&w=900&h=900"

            Glide.with(imgAvatar.context)
                .load(hdUrl)
                .override(900, 900)
                .format(DecodeFormat.PREFER_ARGB_8888)
                .centerCrop()
                .into(imgAvatar)

            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLikedYouBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<LikedYouUser>) {
        items = newItems
        notifyDataSetChanged()
    }
}
