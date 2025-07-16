package com.chilllabs.giphyapp

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var gifAdapter: GifAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
        setupSearchBar()
    }

    private fun setupRecyclerView() {
        gifAdapter = GifAdapter().apply {
            onItemClick = { gif ->
                Toast.makeText(this@MainActivity, "Selected: ${gif.id}", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<RecyclerView>(R.id.gifRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = gifAdapter
        }
    }

    private fun setupSearchBar() {
        val searchBar = findViewById<EditText>(R.id.searchBar)
        searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchBar.text.toString()
                if (query.isNotEmpty()) {
                    searchGifs(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun searchGifs(query: String) {
        // Здесь будет ваша логика поиска через API
        // Пример:
        // viewModel.searchGifs(query)
        Toast.makeText(this, "Searching: $query", Toast.LENGTH_SHORT).show()
    }

    fun updateGifs(gifs: List<GifData>) {
        gifAdapter.submitList(gifs)
    }
}