package com.autocrop.activities.iodetermination.fragments.croppager.pager

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.ui.elements.recyclerview.BidirectionalRecyclerViewAdapter
import com.w2sv.autocrop.databinding.ImageviewCropBinding

class CropPagerAdapter(private val viewModel: CropPagerViewModel)
    : BidirectionalRecyclerViewAdapter<CropPagerAdapter.CropViewHolder>(viewModel.dataSet){

    class CropViewHolder(binding: ImageviewCropBinding)
        : RecyclerView.ViewHolder(binding.cropIv){
        val imageView: ImageView = itemView as ImageView
    }

    /**
     * Inflates [CropViewHolder]
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder =
        CropViewHolder(
            ImageviewCropBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    /**
     * Defines crop setting wrt [position]
     */
    override fun onBindViewHolder(holder: CropViewHolder, position: Int){
        with(viewModel.dataSet.atCorrespondingPosition(position)){
            holder.imageView.setImageBitmap(crop.bitmap)
            ViewCompat.setTransitionName(holder.imageView, identifier())
        }
    }
}