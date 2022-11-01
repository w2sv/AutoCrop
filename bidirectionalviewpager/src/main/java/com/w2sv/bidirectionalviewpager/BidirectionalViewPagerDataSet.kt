package com.w2sv.bidirectionalviewpager

import androidx.annotation.VisibleForTesting
import com.w2sv.bidirectionalviewpager.livedata.MutableListLiveData
import com.w2sv.bidirectionalviewpager.livedata.UpdateBlockableLiveData
import com.w2sv.bidirectionalviewpager.recyclerview.BidirectionalRecyclerViewAdapter
import com.w2sv.kotlinutils.extensions.toInt
import com.w2sv.kotlinutils.extensions.toNonZeroInt
import java.util.Collections

open class BidirectionalViewPagerDataSet<T>(dataSet: MutableList<T>) :
    MutableListLiveData<T>(dataSet) {

    val livePosition = UpdateBlockableLiveData(0, convertUpdateValue = ::correspondingPosition)
    val liveElement: T get() = get(livePosition.value!!)

    fun initialViewPosition(): Int =
        (BidirectionalRecyclerViewAdapter.N_VIEWS / 2).let {
            it - correspondingPosition(it) + livePosition.value!!
        }

    /**
     * For keeping track of actual order
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var tailPosition: Int = lastIndex
    private val headPosition: Int
        get() = (tailPosition + 1) % size

    // ----------------Position Conversion

    fun correspondingPosition(viewPosition: Int): Int =
        viewPosition % size

    fun atCorrespondingPosition(viewPosition: Int): T =
        get(correspondingPosition(viewPosition))

    // ----------------Element Removal

    /**
     * Determines new view position before element removal
     *
     * @return if removing at tail -> preceding view, otherwise subsequent one
     */
    fun viewPositionIncrement(removePosition: Int): Int =
        (tailPosition != removePosition).toNonZeroInt()

    /**
     * - Removes element at [removePosition]
     * - Rotates collection, such as to make fixed [viewPosition] henceforth correspond to
     *   same element as pre-removal
     * - Resets [tailPosition]
     */
    fun removeAndRealign(removePosition: Int, viewPosition: Int) {
        val positionPostRemoval = correspondingPosition(viewPosition).run {
            if (removePosition < this)
                minus(1)
            else
                this
        }

        removeAt(removePosition)

        val rotationDistance = correspondingPosition(viewPosition) - positionPostRemoval

        Collections.rotate(this, rotationDistance)
        tailPosition = rotatedIndex(
            tailPosition - 1 * (tailPosition >= removePosition).toInt(),
            rotationDistance
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun rotatedIndex(index: Int, distance: Int): Int =
        (index + distance).let {
            if (it < 0)
                size + it
            else
                it
        } % size

    // --------------Page Index Retrieval

    fun pageIndex(position: Int): Int =
        if (position > tailPosition)
            position - headPosition
        else
            position + lastIndex - tailPosition
}