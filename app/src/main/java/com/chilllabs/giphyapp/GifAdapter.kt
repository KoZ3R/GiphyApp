package com.chilllabs.giphyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GifAdapter : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_GIF = 0
        private const val TYPE_LOADING = 1
    }

    var onItemClick: ((GiphyApiService.GifData) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is GiphyApiService.GifData) TYPE_GIF else TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_GIF) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_gif, parent, false)
            GifViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GifViewHolder) {
            holder.bind(getItem(position) as GiphyApiService.GifData)
        }
    }

    inner class GifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.gifImageView)

        fun bind(gif: GiphyApiService.GifData) {
            val url = gif.images.fixed_height.url
            println("Loading GIF URL: $url")
            if (url.isNotEmpty()) {
                Glide.with(itemView)
                    .asGif()
                    .load(url)
                    .into(imageView)
            } else {
                println("Empty or null URL for GIF: ${gif.id}")
            }

            itemView.setOnClickListener {
                onItemClick?.invoke(gif)
            }
        }
    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is GiphyApiService.GifData && newItem is GiphyApiService.GifData) {
                oldItem.id == newItem.id
            } else {
                oldItem === newItem
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is GiphyApiService.GifData && newItem is GiphyApiService.GifData -> {
                    oldItem == newItem // data class имеет корректный equals()
                }
                oldItem is LoadingItem && newItem is LoadingItem -> {
                    true // LoadingItem - синглтон, всегда одинаков
                }
                else -> false
            }
        }
    }

    fun addLoadingFooter() {
        val currentList = currentList.toMutableList()
        currentList.add(LoadingItem)
        submitList(currentList)
    }

    fun removeLoadingFooter() {
        val currentList = currentList.toMutableList()
        if (currentList.lastOrNull() is LoadingItem) {
            currentList.removeAt(currentList.size - 1)
            submitList(currentList)
        }
    }

    object LoadingItem
}