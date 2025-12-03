package com.intern001.dating.presentation.ui.discover.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intern001.dating.R
import com.intern001.dating.domain.model.Photo

class PhotoPagerAdapter(
    private val photos: List<Photo>,
) : RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_page, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivPhoto)

        fun bind(photo: Photo) {
            Glide.with(itemView.context)
                .load(photo.url)
                .centerCrop()
                .placeholder(R.drawable.bg_photo_frame)
                .into(imageView)
        }
    }
}
