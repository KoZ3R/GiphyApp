package com.chilllabs.giphyapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var gifAdapter: GifAdapter
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
        setupSearchBar()
        setupScrollToTopButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        gifAdapter = GifAdapter().apply {
            onItemClick = { gif ->
                println("Clicked GIF: ${gif.id}")
                val intent = Intent(this@MainActivity, GifDetailActivity::class.java).apply {
                    putExtra("GIF_ID", gif.id)
                    putExtra("GIF_TITLE", gif.title)
                    putExtra("GIF_URL", gif.images.fixed_height.url)
                }
                startActivity(intent)
            }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.gifRecyclerView)
        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 3 else 2
        recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        recyclerView.adapter = gifAdapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, this))

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Управление видимостью кнопки "Вверх"
                val scrollToTopButton = findViewById<FloatingActionButton>(R.id.scroll_to_top_button)
                scrollToTopButton.visibility = if (firstVisibleItemPosition >= 4) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Пагинация
                if (!viewModel.isLoading.value!! && totalItemCount > 0 && (visibleItemCount + firstVisibleItemPosition >= totalItemCount - 5)) {
                    println("Triggering load more GIFs, visible: $visibleItemCount, total: $totalItemCount, first: $firstVisibleItemPosition")
                    gifAdapter.addLoadingFooter()
                    viewModel.loadMoreGifs()
                }
            }
        })
    }

    private fun setupSearchBar() {
        val searchBar = findViewById<GiphySearchBar>(R.id.searchBar)
        searchBar.onSearchQueryChanged = { query ->
            println("Received search query in MainActivity: $query")
            viewModel.searchGifs(query)
        }
    }

    private fun setupScrollToTopButton() {
        val scrollToTopButton = findViewById<FloatingActionButton>(R.id.scroll_to_top_button)
        scrollToTopButton.setOnClickListener {
            println("Scroll to top clicked")
            findViewById<RecyclerView>(R.id.gifRecyclerView).smoothScrollToPosition(0)
            scrollToTopButton.visibility = android.view.View.GONE
        }
    }

    private fun observeViewModel() {
        viewModel.gifs.observe(this) { gifs ->
            println("Updating RecyclerView with ${gifs.size} GIFs, adapter item count: ${gifAdapter.itemCount}, submitting list")
            gifAdapter.submitList(gifs.toList()) { // Callback для проверки завершения
                println("List submitted, new adapter item count: ${gifAdapter.itemCount}")
            }
            gifAdapter.removeLoadingFooter()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            println("Loading state changed: $isLoading")
            findViewById<android.widget.ProgressBar>(R.id.loadingProgressBar).visibility =
                if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                println("Error received: $error")
                Snackbar.make(findViewById(R.id.gifRecyclerView), error, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}