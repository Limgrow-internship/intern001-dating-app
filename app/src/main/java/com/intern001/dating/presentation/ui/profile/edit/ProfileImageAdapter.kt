package com.intern001.dating.presentation.ui.profile.edit

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intern001.dating.R

class ProfileImageAdapter(
    private val images: List<String>
) : RecyclerView.Adapter<ProfileImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imageItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = images[position]
        Log.d("ProfileImageAdapter", "Loading URL: $url")

        Glide.with(holder.img.context)
            .load(url)
            .centerCrop()
            .placeholder(R.drawable.co4la) 
            .error(R.drawable.ob2img) 
            .into(holder.img)
    }
}
