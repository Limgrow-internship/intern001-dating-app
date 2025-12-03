package com.intern001.dating.presentation.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.intern001.dating.R
import com.intern001.dating.databinding.ItemMatchBinding
import com.intern001.dating.domain.model.MatchList

class MatchAdapter(
    val onClick: (MatchList) -> Unit = {},
) : ListAdapter<MatchList, MatchAdapter.MatchViewHolder>(DIFF) {

    class MatchViewHolder(private val binding: ItemMatchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MatchList, onClick: (MatchList) -> Unit) {
            binding.tvNameAge.text = "${item.matchedUser.name}, ${item.matchedUser.age ?: ""}"
            binding.tvLocation.text = item.matchedUser.city.orEmpty()
            val radius = (30 * binding.imvAvatar.resources.displayMetrics.density).toInt()
            Glide.with(binding.imvAvatar)
                .load(item.matchedUser.avatarUrl)
                .placeholder(R.drawable.bg_avatar_round)
                .centerCrop()
                .transform(RoundedCorners(radius))
                .into(binding.imvAvatar)
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MatchList>() {
            override fun areItemsTheSame(oldItem: MatchList, newItem: MatchList): Boolean = oldItem.matchId == newItem.matchId

            override fun areContentsTheSame(oldItem: MatchList, newItem: MatchList): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }
}
