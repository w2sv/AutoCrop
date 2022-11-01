package com.w2sv.autocrop.activities.iodetermination.fragments.croppager

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.w2sv.autocrop.CropBundle
import com.w2sv.autocrop.databinding.ImageviewCropBinding
import com.w2sv.autocrop.utils.VoidFun
import com.w2sv.bidirectionalviewpager.BidirectionalViewPagerDataSet
import com.w2sv.bidirectionalviewpager.livedata.UpdateBlockableLiveData
import com.w2sv.bidirectionalviewpager.makeViewRemover
import com.w2sv.bidirectionalviewpager.recyclerview.BidirectionalRecyclerViewAdapter
import com.w2sv.kotlinutils.delegates.Consumable

/**
 * Proxy (=wrapper) for unextendable [viewPager2], providing additional functionality
 */
class CropPager(private val viewPager2: ViewPager2, private val dataSet: BidirectionalViewPagerDataSet<CropBundle>) {

    class Adapter(
        private val dataSet: BidirectionalViewPagerDataSet<CropBundle>,
        offscreenPageLimit: Int
    ) : BidirectionalRecyclerViewAdapter<Adapter.ViewHolder>(
        dataSet, offscreenPageLimit
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

        /**
         * Defines crop setting wrt [position]
         */
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            dataSet.atCorrespondingPosition(position).let {
                with(holder.imageView) {
                    setImageBitmap(it.crop.bitmap)
                    ViewCompat.setTransitionName(this, it.identifier())
                }
            }
        }
    }

    private class OnPageChangeCallback(private val livePosition: UpdateBlockableLiveData<Int>) : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            livePosition.update(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)

            if (state == ViewPager.SCROLL_STATE_IDLE)
                onScrollStateIdle?.invoke()
        }

        var onScrollStateIdle by Consumable<VoidFun>()
    }

    private val onPageChangeCallback: OnPageChangeCallback

    init {
        with(viewPager2) {
            adapter = Adapter(dataSet, offscreenPageLimit)
            onPageChangeCallback = OnPageChangeCallback(dataSet.livePosition)
                .apply {
                    registerOnPageChangeCallback(this)
                }
            setCurrentItem(
                dataSet.initialViewPosition(),
                false
            )
        }
    }

    fun removeView(dataSetPosition: Int) {
        onPageChangeCallback.onScrollStateIdle = viewPager2.makeViewRemover(dataSetPosition, dataSet)
    }
}