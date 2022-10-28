package com.w2sv.autocrop.utils.android.extensions

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

val ViewPager2.recyclerView: RecyclerView
    get() = getChildAt(0) as RecyclerView