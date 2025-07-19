package com.chilllabs.giphyapp

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val spanCount: Int,
    context: Context,
    spacingDp: Float = 2f
) : RecyclerView.ItemDecoration() {

    private val spacingPx: Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        spacingDp,
        context.resources.displayMetrics
    ).toInt()

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (parent.getChildViewHolder(view) is GifAdapter.LoadingViewHolder) {
            outRect.set(0, 0, 0, 0)
            return
        }

        val column = position % spanCount
        outRect.left = spacingPx - (column * spacingPx / spanCount)
        outRect.right = ((column + 1) * spacingPx / spanCount)
        outRect.top = if (position < spanCount) 0 else spacingPx
        outRect.bottom = spacingPx
    }
}