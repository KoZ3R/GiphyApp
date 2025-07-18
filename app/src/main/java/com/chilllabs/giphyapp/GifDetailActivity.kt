package com.chilllabs.giphyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class GifDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_detail)

        val gifId = intent.getStringExtra("GIF_ID") ?: ""
        val gifTitle = intent.getStringExtra("GIF_TITLE") ?: "No Title"
        val gifUrl = intent.getStringExtra("GIF_URL") ?: ""

        findViewById<TextView>(R.id.gifTitle).text = gifTitle
        val imageView = findViewById<ImageView>(R.id.gifImageView)
        if (gifUrl.isNotEmpty()) {
            Glide.with(this)
                .asGif()
                .load(gifUrl)
                .into(imageView)
        }
    }
}