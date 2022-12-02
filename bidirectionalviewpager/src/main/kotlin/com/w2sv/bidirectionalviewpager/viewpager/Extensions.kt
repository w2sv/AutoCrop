package com.w2sv.bidirectionalviewpager.viewpager

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

val ViewPager2.recyclerView: RecyclerView
    get() = getChildAt(0) as RecyclerView

@Suppress("UNCHECKED_CAST")
fun <VH : RecyclerView.ViewHolder> ViewPager2.currentViewHolder(): VH? =
    (recyclerView.findViewHolderForAdapterPosition(currentItem) as? VH)