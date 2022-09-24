package com.autocrop.activities.examination.fragments.viewpager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.autocrop.dataclasses.CropBundle
import com.autocrop.ui.elements.recyclerview.BidirectionalRecyclerViewAdapter
import com.w2sv.autocrop.R

fun CropBundle.transitionName(): String =
    hashCode().toString()

class CropPagerAdapter(private val viewModel: ViewPagerViewModel)
    : BidirectionalRecyclerViewAdapter<CropPagerAdapter.CropViewHolder>(viewModel.dataSet){

    class CropViewHolder(view: View)
        : RecyclerView.ViewHolder(view) {
        val cropImageView: ImageView = view.findViewById(R.id.crop_iv)
    }

    /**
     * Inflates [CropViewHolder]
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder =
        CropViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.examination_cropimageviewholder, parent, false)
        )

    /**
     * Defines crop setting wrt [position]
     */
    override fun onBindViewHolder(holder: CropViewHolder, position: Int){
        holder.cropImageView.apply{
            val cropBundle = viewModel.dataSet.atCorrespondingPosition(position)

            setImageBitmap(cropBundle.crop.bitmap)
            ViewCompat.setTransitionName(this, cropBundle.transitionName())
        }
    }
}