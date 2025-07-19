package com.chilllabs.giphyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GifAdapter : ListAdapter<GiphyApiService.GifData, RecyclerView.ViewHolder>(GifDiffCallback()) {

    var onItemClick: ((GiphyApiService.GifData) -> Unit)? = null
    private var isLoadingFooterAdded = false

    companion object {
        private const val TYPE_GIF = 0
        private const val TYPE_LOADING = 1
    }

    class GifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.gifImageView)
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_GIF) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gif, parent, false)
            GifViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_GIF) {
            val gif = getItem(position) as GiphyApiService.GifData
            val gifHolder = holder as GifViewHolder
            println("Binding GIF at position $position: ${gif.id}, URL: ${gif.images.fixed_height.url}")
            gifHolder.imageView.contentDescription = holder.itemView.context.getString(R.string.gif_description, gif.title.takeIf { it.isNotEmpty() } ?: "Unnamed GIF")
            Glide.with(gifHolder.imageView.context)
                .asGif()
                .load(gif.images.fixed_height.url)
                .error(R.drawable.error_placeholder)
                .into(gifHolder.imageView)
            gifHolder.itemView.setOnClickListener {
                onItemClick?.invoke(gif)
            }
        } else {
            println("Binding loading footer at position $position")
        }
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount() + if (isLoadingFooterAdded) 1 else 0
        println("Adapter item count: $count, base list size: ${super.getItemCount()}")
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoadingFooterAdded && position == super.getItemCount()) TYPE_LOADING else TYPE_GIF
    }

    fun addLoadingFooter() {
        if (!isLoadingFooterAdded) {
            isLoadingFooterAdded = true
            notifyItemInserted(super.getItemCount())
        }
    }

    fun removeLoadingFooter() {
        if (isLoadingFooterAdded) {
            isLoadingFooterAdded = false
            notifyItemRemoved(super.getItemCount())
        }
    }
}

class GifDiffCallback : DiffUtil.ItemCallback<GiphyApiService.GifData>() {
    override fun areItemsTheSame(oldItem: GiphyApiService.GifData, newItem: GiphyApiService.GifData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: GiphyApiService.GifData, newItem: GiphyApiService.GifData): Boolean {
        return oldItem == newItem
    }
}