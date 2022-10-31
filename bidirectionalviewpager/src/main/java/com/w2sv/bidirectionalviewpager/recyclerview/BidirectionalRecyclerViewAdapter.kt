package com.w2sv.bidirectionalviewpager.recyclerview

import androidx.recyclerview.widget.RecyclerView

abstract class BidirectionalRecyclerViewAdapter<VH : RecyclerView.ViewHolder>(private val dataSet: Collection<Any>) : ExtendedRecyclerViewAdapter<VH>() {

    companion object {
        const val N_VIEWS = Int.MAX_VALUE
    }

    override fun getItemCount(): Int =
        if (dataSet.size == 1) 1 else N_VIEWS

    fun notifyItemChanged(position: Int, dataSetSize: Int) {
        super.notifyItemChanged(position)

        if (dataSetSize == 2) {
            super.notifyItemChanged(position - 2)
            super.notifyItemChanged(position + 2)
        }
    }
}