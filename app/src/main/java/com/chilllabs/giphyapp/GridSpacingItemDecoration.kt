package com.chilllabs.giphyapp

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacingDp: Int,
    private val includeEdge: Boolean = true
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacingDp - column * spacingDp / spanCount
            outRect.right = (column + 1) * spacingDp / spanCount
            outRect.top = if (position < spanCount) spacingDp else 0
            outRect.bottom = spacingDp
        } else {
            outRect.left = column * spacingDp / spanCount
            outRect.right = spacingDp - (column + 1) * spacingDp / spanCount
            outRect.top = if (position < spanCount) 0 else spacingDp
            outRect.bottom = 0
        }
    }

    companion object {
        fun dpToPx(dp: Int, context: Context): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }
    }
}