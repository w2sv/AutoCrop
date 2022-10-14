package com.w2sv.bidirectionalviewpager.recyclerview

import androidx.recyclerview.widget.RecyclerView

abstract class ExtendedRecyclerViewAdapter<VH: RecyclerView.ViewHolder>
    : RecyclerView.Adapter<VH>(){

    fun resetCachedViewsAround(position: Int){
        val nCachedViewsToEitherSide = 3

        notifyItemRangeChanged(
            position - nCachedViewsToEitherSide,
            nCachedViewsToEitherSide * 2 + 1
        )
    }
}