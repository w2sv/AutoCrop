package com.w2sv.autocrop.ui.screen.pager.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import com.w2sv.autocrop.databinding.CropImageViewBinding
import com.w2sv.autocrop.ui.util.nonNullValue
import com.w2sv.bidirectionalviewpager.BidirectionalViewPagerDataSet
import com.w2sv.bidirectionalviewpager.ViewRemovableBidirectionalViewPager2
import com.w2sv.bidirectionalviewpager.livedata.UpdateBlockableLiveData
import com.w2sv.bidirectionalviewpager.recyclerview.BidirectionalRecyclerViewAdapter
import com.w2sv.bidirectionalviewpager.recyclerview.ImageViewHolder
import com.w2sv.cropbundle.CropBundle

/**
 * Proxy for unextendable [ViewPager2].
 */
class CropPagerWrapper(
    val pager: ViewPager2,
    private val dataSet: DataSet,
    onClickListener: (View) -> Unit,
    onLongClickListener: (View) -> Boolean,
) : ViewRemovableBidirectionalViewPager2<CropBundle>(pager, dataSet) {

    class DataSet(dataSet: MutableList<CropBundle>) : BidirectionalViewPagerDataSet<CropBundle>(dataSet) {
        val livePosition: UpdateBlockableLiveData<Int> = UpdateBlockableLiveData(0, ::getCorrespondingPosition)
        val liveElement get() = get(livePosition.nonNullValue)
    }

    init {
        with(pager) {
            adapter =
                object : BidirectionalRecyclerViewAdapter<BidirectionalViewPagerDataSet<CropBundle>, ImageViewHolder>(
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
                                ViewCompat.setTransitionName(this, cropBundle.identifier)
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
                dataSet.initialViewPosition(dataSet.livePosition.nonNullValue),
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