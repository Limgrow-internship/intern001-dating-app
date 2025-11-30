package com.intern001.dating.presentation.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intern001.dating.databinding.ItemLikedYouBinding

class LikedYouAdapter (
    private val items: List<UserMini>,
    private val viewModel: MatchStatusViewModel
) : RecyclerView.Adapter<LikedYouAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLikedYouBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLikedYouBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = items[position]

        holder.binding.apply {
            txtNameAge.text = "${user.displayName}, ${user.age}"
            txtLocation.text = user.city

            Glide.with(imgAvatar.context)
                .load(user.avatar)
                .into(imgAvatar)
        }

        viewModel.loadMatchStatus(user.userId)

        viewModel.status.observeForever { status ->
            if (status != null && status.targetProfile != null) {

                if (status.userLiked) {
                    holder.binding.imgMatchIcon.setImageResource(R.drawable.ic_like)
                }

                if (status.matched) {
                    holder.binding.imgMatchIcon.setImageResource(R.drawable.ic_person_double)
                }
            }
        }
    }

    override fun getItemCount() = items.size
}
