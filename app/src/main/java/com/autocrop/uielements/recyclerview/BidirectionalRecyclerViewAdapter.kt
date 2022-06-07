package com.autocrop.uielements.recyclerview

import androidx.recyclerview.widget.RecyclerView

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