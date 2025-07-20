package com.chilllabs.giphyapp

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GiphySearchBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    var onSearchQueryChanged: ((String) -> Unit)? = null
    private var searchJob: Job? = null

    init {
        setupView()
    }

    private fun setupView() {
        background = AppCompatResources.getDrawable(context, R.drawable.search_bar_bg)
        hint = "Search GIFs..."
        textSize = 16f
        setTextColor(ContextCompat.getColor(context, R.color.search_text))
        setHintTextColor(ContextCompat.getColor(context, R.color.white))
        inputType = android.text.InputType.TYPE_CLASS_TEXT
        minHeight = 48.dpToPx(context)
        maxWidth = 360.dpToPx(context)
        contentDescription = context.getString(R.string.search_bar_description)
        setPadding(
            resources.getDimensionPixelSize(R.dimen.search_bar_padding_start), // 8dp
            paddingTop,
            paddingEnd,
            paddingBottom
        )

        addTextChangedListener { editable ->
            val query = editable?.toString()?.trim() ?: ""
            searchJob?.cancel()
            searchJob = MainScope().launch {
                delay(500L)
                if (query.isNotEmpty()) {
                    println("Search query emitted: $query")
                    onSearchQueryChanged?.invoke(query)
                } else {
                    println("Search query is empty, skipping")
                }
            }
        }
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}