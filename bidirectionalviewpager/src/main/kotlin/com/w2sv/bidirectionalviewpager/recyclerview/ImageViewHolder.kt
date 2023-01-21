package com.w2sv.bidirectionalviewpager.recyclerview

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

open class ImageViewHolder(imageView: ImageView): RecyclerView.ViewHolder(imageView) {
    val imageView: ImageView = itemView as ImageView
}