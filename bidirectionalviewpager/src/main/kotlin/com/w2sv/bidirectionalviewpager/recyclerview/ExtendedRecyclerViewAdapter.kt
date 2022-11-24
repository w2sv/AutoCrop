package com.w2sv.bidirectionalviewpager.recyclerview

import androidx.recyclerview.widget.RecyclerView

abstract class ExtendedRecyclerViewAdapter<VH : RecyclerView.ViewHolder>(private val offscreenPageLimit: Int) : RecyclerView.Adapter<VH>() {

    fun resetCachedViewsAround(position: Int) {
        notifyItemRangeChanged(
            position - offscreenPageLimit,
            changedItemCount
        )
    }

    private val changedItemCount by lazy {
        offscreenPageLimit * 2 + 1
    }
}