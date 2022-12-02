package com.w2sv.autocrop.activities.cropexamination.fragments.croppager

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.w2sv.autocrop.cropbundle.CropBundle
import com.w2sv.autocrop.databinding.ImageviewCropBinding
import com.w2sv.bidirectionalviewpager.BidirectionalViewPagerDataSet
import com.w2sv.bidirectionalviewpager.recyclerview.BidirectionalRecyclerViewAdapter
import com.w2sv.bidirectionalviewpager.viewpager.ExtendedOnPageChangeCallback
import com.w2sv.bidirectionalviewpager.viewpager.makeRemoveView

/**
 * Proxy (=wrapper) for unextendable [viewPager2], providing additional functionality
 */
class CropPager(private val viewPager2: ViewPager2, private val dataSet: BidirectionalViewPagerDataSet<CropBundle>) {

    class Adapter(
        dataSet: BidirectionalViewPagerDataSet<CropBundle>,
        offscreenPageLimit: Int
    ) : BidirectionalRecyclerViewAdapter<BidirectionalViewPagerDataSet<CropBundle>, Adapter.ViewHolder>(
        dataSet,
        offscreenPageLimit
    ) {

        class ViewHolder(binding: ImageviewCropBinding) : RecyclerView.ViewHolder(binding.cropIv) {
            val imageView: ImageView get() = itemView as ImageView
        }

        /**
         * Inflates [ViewHolder]
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(
                ImageviewCropBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            dataSet.atCorrespondingPosition(position).let {
                with(holder.imageView) {
                    setImageBitmap(it.crop.bitmap)
                    ViewCompat.setTransitionName(this, it.identifier())
                }
            }
        }
    }

    init {
        with(viewPager2) {
            adapter = Adapter(dataSet, 3)
            registerOnPageChangeCallback(onPageChangeCallback)
            setCurrentItem(
                dataSet.initialViewPosition(),
                false
            )
        }
    }

    private val onPageChangeCallback = object: ExtendedOnPageChangeCallback(){
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            dataSet.livePosition.update(position)
        }
    }

    fun removeView(dataSetPosition: Int) {
        onPageChangeCallback.setRemoveView(viewPager2.makeRemoveView(dataSetPosition, dataSet))
    }
}