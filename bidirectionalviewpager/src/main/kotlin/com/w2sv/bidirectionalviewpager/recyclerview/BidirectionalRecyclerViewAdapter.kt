package com.w2sv.bidirectionalviewpager.recyclerview

import androidx.recyclerview.widget.RecyclerView

abstract class BidirectionalRecyclerViewAdapter<DS : Collection<Any>, VH : RecyclerView.ViewHolder>(
    val dataSet: DS,
    offscreenPageLimit: Int
) : ExtendedRecyclerViewAdapter<VH>(offscreenPageLimit) {

    companion object {
        const val N_VIEWS = Int.MAX_VALUE
    }

    override fun getItemCount(): Int =
        if (dataSet.size == 1)
            1
        else
            N_VIEWS

    fun notifyItemChangedOverride(position: Int) {
        super.notifyItemChanged(position)

        if (dataSet.size == 2) {
            super.notifyItemChanged(position - 2)
            super.notifyItemChanged(position + 2)
        }
    }
}