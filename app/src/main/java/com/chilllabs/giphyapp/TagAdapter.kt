package com.chilllabs.giphyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TagAdapter(
    private val tags: List<String>,
    private val onTagClick: (String) -> Unit
) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tagTextView: TextView = itemView.findViewById(R.id.tagTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]
        holder.tagTextView.text = "#$tag"
        holder.tagTextView.setOnClickListener {
            onTagClick(tag)
        }
    }

    override fun getItemCount(): Int = tags.size
}