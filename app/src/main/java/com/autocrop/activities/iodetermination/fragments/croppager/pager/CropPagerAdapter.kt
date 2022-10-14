package com.autocrop.activities.iodetermination.fragments.croppager.pager

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.databinding.ImageviewCropBinding
import com.w2sv.bidirectionalviewpager.recyclerview.BidirectionalRecyclerViewAdapter

class CropPagerAdapter(private val viewModel: CropPagerViewModel)
    : BidirectionalRecyclerViewAdapter<CropPagerAdapter.CropViewHolder>(viewModel.dataSet){

    class CropViewHolder(binding: ImageviewCropBinding)
        : RecyclerView.ViewHolder(binding.cropIv){
        val imageView: ImageView get() = itemView as ImageView
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
        viewModel.dataSet.atCorrespondingPosition(position).let {
            with(holder.imageView){
                setImageBitmap(it.crop.bitmap)
                ViewCompat.setTransitionName(this, it.identifier())
            }
        }
    }
}