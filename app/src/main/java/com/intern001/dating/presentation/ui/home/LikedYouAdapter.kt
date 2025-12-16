package com.intern001.dating.presentation.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.intern001.dating.R
import com.intern001.dating.databinding.ItemLikedYouBinding
import com.intern001.dating.domain.model.LikedYouUser

class LikedYouAdapter(
    private val onItemClick: (LikedYouUser) -> Unit,
) : ListAdapter<LikedYouUser, LikedYouAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(val binding: ItemLikedYouBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LikedYouUser) = with(binding) {
            txtNameAge.text = item.displayName ?: "Chưa cập nhật"
            txtLocation.text = item.city ?: "Chưa cập nhật"

            val imageUrl = when {
                !item.avatar.isNullOrBlank() -> item.avatar
                !item.picture.isNullOrBlank() -> item.picture
                else -> null
            }

            if (imageUrl == null) {
                Glide.with(imgAvatar.context)
                    .load(R.drawable.bg_avatar_round)
                    .centerCrop()
                    .into(imgAvatar)
            } else {
                val hdUrl = "$imageUrl?q_auto:best&f_auto&c_fill&w=900&h=900"

                Glide.with(imgAvatar.context)
                    .load(hdUrl)
                    .override(900, 900)
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .centerCrop()
                    .error(R.drawable.bg_avatar_round)
                    .into(imgAvatar)
            }

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
                false,
            ),
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LikedYouUser>() {
            override fun areItemsTheSame(oldItem: LikedYouUser, newItem: LikedYouUser): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: LikedYouUser, newItem: LikedYouUser): Boolean {
                return oldItem == newItem
            }
        }
    }
}
