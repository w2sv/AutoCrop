package com.autocrop.uielements

import androidx.recyclerview.widget.RecyclerView

abstract class ExtendedRecyclerViewAdapter<VH: RecyclerView.ViewHolder>
    : RecyclerView.Adapter<VH>(){

    fun resetCachedViewsAround(position: Int){
        val nCachedViewsToEitherSide = 3

        notifyItemRangeChanged(position - nCachedViewsToEitherSide, nCachedViewsToEitherSide * 2 + 1)
    }
}

abstract class BidirectionalRecyclerViewAdapter<VH: RecyclerView.ViewHolder>
    : ExtendedRecyclerViewAdapter<VH>(){

    fun notifyItemChanged(position: Int, dataSetSize: Int){
        super.notifyItemChanged(position)

        if (dataSetSize == 2){
            super.notifyItemChanged(position - 2)
            super.notifyItemChanged(position + 2)
        }
    }
}