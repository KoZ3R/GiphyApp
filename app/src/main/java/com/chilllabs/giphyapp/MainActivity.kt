package com.chilllabs.giphyapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var gifAdapter: GifAdapter
    private lateinit var tagAdapter: TagAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchBar = findViewById<GiphySearchBar>(R.id.searchBar)
        val gifRecyclerView = findViewById<RecyclerView>(R.id.gifRecyclerView)
        val tagRecyclerView = findViewById<RecyclerView>(R.id.tagRecyclerView)
        val loadingProgressBar = findViewById<ProgressBar>(R.id.loadingProgressBar)
        val scrollToTopButton = findViewById<FloatingActionButton>(R.id.scroll_to_top_button)

        // Настройка адаптера GIF
        gifAdapter = GifAdapter()
        gifRecyclerView.layoutManager = GridLayoutManager(this, 2)
        gifRecyclerView.adapter = gifAdapter
        gifRecyclerView.addItemDecoration(GridSpacingItemDecoration(2, 2.dpToPx(this)))

        // Настройка адаптера тегов
        val tags = listOf("cats", "dogs", "funny", "memes")
        tagAdapter = TagAdapter(tags) { tag ->
            searchBar.setText(tag)
            viewModel.searchGifs(tag)
        }
        tagRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        tagRecyclerView.adapter = tagAdapter

        // Настройка поиска
        searchBar.onSearchQueryChanged = { query ->
            viewModel.searchGifs(query)
        }

        // Наблюдение за ViewModel
        viewModel.gifs.observe(this) { gifs ->
            gifAdapter.submitList(gifs)
            loadingProgressBar.visibility = if (gifs.isEmpty() && viewModel.isLoading.value == true) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                gifAdapter.addLoadingFooter()
            } else {
                gifAdapter.removeLoadingFooter()
            }
        }

        // Кнопка "Вверх"
        gifRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                scrollToTopButton.visibility = if (firstVisibleItemPosition >= 4) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                if (!recyclerView.canScrollVertically(1) && !viewModel.isLoading.value!!) {
                    viewModel.loadMoreGifs()
                }
            }
        })

        scrollToTopButton.setOnClickListener {
            gifRecyclerView.smoothScrollToPosition(0)
        }

        // Начальная загрузка
        viewModel.searchGifs("")
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}