package com.chilllabs.giphyapp

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

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
                Toast.makeText(
                    this@MainActivity,
                    "Selected GIF: ${gif.id}",
                    Toast.LENGTH_SHORT
                ).show()
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
                val query = searchBar.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchGifs(query)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Please enter a search term",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            } else {
                false
            }
        }
    }

    private fun searchGifs(query: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.giphyService.searchGifs(query = query)
                if (response.data.isNotEmpty()) {
                    gifAdapter.submitList(response.data)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "No GIFs found for '$query'",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }
}