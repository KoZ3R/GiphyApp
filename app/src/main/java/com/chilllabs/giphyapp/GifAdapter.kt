package com.chilllabs.giphyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GifAdapter : ListAdapter<GiphyApiService.GifData, GifAdapter.GifViewHolder>(DiffCallback()) {

    var onItemClick: ((GiphyApiService.GifData) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gif, parent, false)
        return GifViewHolder(view)
    }

    override fun onBindViewHolder(holder: GifViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.gifImageView)

        fun bind(gif: GiphyApiService.GifData) {
            println("Loading GIF URL: ${gif.images.fixedHeight.url}")
            Glide.with(itemView)
                .asGif()
                .load(gif.images.fixedHeight.url)
                .error(R.drawable.error_placeholder)
                .into(imageView)

            itemView.setOnClickListener {
                onItemClick?.invoke(gif)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<GiphyApiService.GifData>() {
        override fun areItemsTheSame(oldItem: GiphyApiService.GifData, newItem: GiphyApiService.GifData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GiphyApiService.GifData, newItem: GiphyApiService.GifData): Boolean {
            return oldItem == newItem
        }
    }
}