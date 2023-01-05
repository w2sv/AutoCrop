package com.w2sv.autocrop.activities.examination.fragments.croppager

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.w2sv.autocrop.cropbundle.CropBundle
import com.w2sv.autocrop.databinding.ImageviewCropBinding
import com.w2sv.bidirectionalviewpager.BidirectionalViewPagerDataSet
import com.w2sv.bidirectionalviewpager.ViewRemovableBidirectionalViewPager2
import com.w2sv.bidirectionalviewpager.livedata.UpdateBlockableLiveData
import com.w2sv.bidirectionalviewpager.recyclerview.BidirectionalRecyclerViewAdapter

/**
 * Proxy for non extendable [viewPager2]
 */
class CropPager(
    private val viewPager2: ViewPager2,
    val dataSet: DataSet
) : ViewRemovableBidirectionalViewPager2<CropBundle>(viewPager2, dataSet) {

    class DataSet(dataSet: MutableList<CropBundle>) : BidirectionalViewPagerDataSet<CropBundle>(dataSet) {

        val livePosition: UpdateBlockableLiveData<Int> = UpdateBlockableLiveData(0, ::getCorrespondingPosition)
        val liveElement get() = get(livePosition.value!!)

        fun initialViewPosition(): Int =
            initialViewPosition(livePosition.value!!)
    }

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

    val adapter = Adapter(dataSet, 3)

    init {
        with(viewPager2) {
            adapter = this@CropPager.adapter
            registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    dataSet.livePosition.update(position)
                }
            })
            setCurrentItem(
                dataSet.initialViewPosition(),
                false
            )
        }
    }

    fun scrollToNextViewAndRemoveCurrent(dataSetPosition: Int) {
        dataSet.livePosition.blockSubsequentUpdate()

        super.scrollToNextViewAndRemoveCurrent(dataSetPosition) {
            dataSet.livePosition.update(it)
        }
    }
}