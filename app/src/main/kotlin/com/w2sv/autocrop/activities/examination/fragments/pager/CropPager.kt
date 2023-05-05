package com.w2sv.autocrop.activities.examination.fragments.pager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import com.w2sv.autocrop.databinding.CropImageViewBinding
import com.w2sv.bidirectionalviewpager.BidirectionalViewPagerDataSet
import com.w2sv.bidirectionalviewpager.ViewRemovableBidirectionalViewPager2
import com.w2sv.bidirectionalviewpager.livedata.UpdateBlockableLiveData
import com.w2sv.bidirectionalviewpager.recyclerview.BidirectionalRecyclerViewAdapter
import com.w2sv.bidirectionalviewpager.recyclerview.ImageViewHolder
import com.w2sv.cropbundle.CropBundle

/**
 * Proxy for non extendable [ViewPager2].
 */
class CropPager(
    val pager: ViewPager2,
    private val dataSet: DataSet,
    private val onClickListener: (View) -> Unit,
    private val onLongClickListener: (View) -> Boolean,
) : ViewRemovableBidirectionalViewPager2<CropBundle>(pager, dataSet) {

    class DataSet(dataSet: MutableList<CropBundle>) : BidirectionalViewPagerDataSet<CropBundle>(dataSet) {
        val livePosition: UpdateBlockableLiveData<Int> = UpdateBlockableLiveData(0, ::getCorrespondingPosition)
        val liveElement get() = get(livePosition.value!!)
    }

    init {
        pager.initialize()
    }

    private fun ViewPager2.initialize() {
        adapter = object : BidirectionalRecyclerViewAdapter<BidirectionalViewPagerDataSet<CropBundle>, ImageViewHolder>(
            dataSet,
            3
        ) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder =
                ImageViewHolder(
                    CropImageViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                        .cropIv
                        .apply {
                            setOnClickListener(onClickListener)
                            setOnLongClickListener(onLongClickListener)
                        }
                )

            override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
                dataSet.atCorrespondingPosition(position).let { cropBundle ->
                    with(holder.imageView) {
                        setImageBitmap(cropBundle.crop.bitmap)
                        ViewCompat.setTransitionName(this, cropBundle.identifier())
                    }
                }
            }
        }
        registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                dataSet.livePosition.update(position)
            }
        })
        setCurrentItem(
            dataSet.livePosition.value!!,
            false
        )
    }

    fun scrollToNextViewAndRemoveCurrent(dataSetPosition: Int) {
        dataSet.livePosition.blockSubsequentUpdate()

        super.scrollToNextViewAndRemoveCurrent(dataSetPosition) {
            dataSet.livePosition.update(it)
        }
    }
}