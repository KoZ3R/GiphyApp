package com.chilllabs.giphyapp

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
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
        setTextColor(Color.BLACK)
        inputType = android.text.InputType.TYPE_CLASS_TEXT
        minHeight = 48.dpToPx(context) // Минимальная высота для области касания
        maxWidth = 360.dpToPx(context) // Ограничение максимальной ширины
        contentDescription = context.getString(R.string.search_bar_description) // Для доступности
        // Добавляем отступ слева для текста
        setPadding(
            resources.getDimensionPixelSize(R.dimen.search_bar_padding_start), // 8dp
            paddingTop,
            paddingEnd,
            paddingBottom
        )

        // Настройка автопоиска
        addTextChangedListener { editable ->
            val query = editable?.toString()?.trim() ?: ""
            searchJob?.cancel() // Отменяем предыдущий поиск
            searchJob = MainScope().launch {
                delay(500L) // Задержка 500 мс
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