package com.chilllabs.giphyapp

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.net.URLEncoder

class GiphySearchBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var gifAdapter: GifAdapter? = null
    private var lifecycleOwner: LifecycleOwner? = null

    init {
        setupView()
    }

    private fun setupView() {
        background = context.getDrawable(R.drawable.search_bar_bg)
        hint = "Search GIFs..."
        textSize = 16f
        setTextColor(Color.BLACK)
        imeOptions = EditorInfo.IME_ACTION_SEARCH
        inputType = EditorInfo.TYPE_CLASS_TEXT

        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    fun setupWithRecyclerView(owner: LifecycleOwner, adapter: GifAdapter) {
        this.lifecycleOwner = owner
        this.gifAdapter = adapter
    }

    private fun performSearch() {
        val query = text.toString().trim()
        if (query.isEmpty()) {
            Toast.makeText(context, "Введите запрос для поиска", Toast.LENGTH_SHORT).show()
            return
        }

        val encodedQuery = URLEncoder.encode(query, "UTF-8").replace("+", "%20")
        lifecycleOwner?.lifecycleScope?.launch {
            try {
                println("Sending request for query: $encodedQuery")
                val response = RetrofitClient.giphyService.searchGifs(
                    apiKey = RetrofitClient.API_KEY,
                    query = encodedQuery
                )
                println("Received response: $response")
                if (response.data.isNotEmpty()) {
                    gifAdapter?.submitList(response.data)
                } else {
                    Toast.makeText(context, "GIF для '$query' не найдены", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                Toast.makeText(context, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}