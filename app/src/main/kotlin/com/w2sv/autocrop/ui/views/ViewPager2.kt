package com.w2sv.autocrop.ui.views

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

@Suppress("UNCHECKED_CAST")
fun <VH : RecyclerView.ViewHolder> ViewPager2.currentViewHolder(): VH? =
    recyclerView.findViewHolderForAdapterPosition(currentItem) as? VH

val ViewPager2.recyclerView: RecyclerView
    get() = getChildAt(0) as RecyclerView

fun ViewPager2.notifyCurrentItemChanged() {
    adapter!!.notifyItemChanged(currentItem)
}