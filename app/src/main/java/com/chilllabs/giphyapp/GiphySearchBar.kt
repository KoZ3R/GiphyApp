// GiphySearchBar.kt
package com.chilllabs.giphyapp

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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
        // Настройка внешнего вида
        background = context.getDrawable(R.drawable.search_bar_bg)
        hint = "Search GIFs..."
        textSize = 16f
        setTextColor(Color.BLACK)
        imeOptions = EditorInfo.IME_ACTION_SEARCH
        inputType = EditorInfo.TYPE_CLASS_TEXT

        // Обработка поиска
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
        if (query.isEmpty()) return

        lifecycleOwner?.lifecycleScope?.launch {
            try {
                val response = RetrofitClient.giphyService.searchGifs(
                    apiKey = RetrofitClient.API_KEY,
                    query = query
                )
                gifAdapter?.updateData(response.data)
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }

    private fun GifAdapter?.updateData(data: List<GifData>) {}
}