package com.autocrop.activities.examination

import androidx.lifecycle.ViewModel
import com.autocrop.CropBundle
import com.autocrop.CropBundleList
import com.autocrop.cropBundleList
import com.autocrop.utils.Index
import com.autocrop.utils.at
import com.autocrop.utils.get
import com.autocrop.utils.rotated
import java.util.*


class ExaminationViewModel(conductAutoScroll: Boolean,
                           longAutoScrollDelay: Boolean): ViewModel() {
    var nSavedCrops = 0

    fun incrementNSavedCrops(by: Int = 1){
        nSavedCrops += by
    }

    val viewPager = ViewPagerViewModel(conductAutoScroll, longAutoScrollDelay)
}

class ViewPagerViewModel(val conductAutoScroll: Boolean,
                         val longAutoScrollDelay: Boolean){

    companion object {
        const val MAX_VIEWS: Int = Int.MAX_VALUE
    }

    val dataSet = ViewPagerDataSet()

    val startPosition: Int = (MAX_VIEWS / 2).run {
        minus(dataSet.correspondingPosition(this))
    }
}

class ViewPagerDataSet : CropBundleList by cropBundleList {

    // -------------Position Trackers

    private var tailHash: Int = cropBundleList.last().hashCode()
    private var tailPosition: Index = cropBundleList.lastIndex
    private var headPosition: Index = 0

    // ----------------Position Conversion

    fun correspondingPosition(viewPosition: Int): Int = viewPosition % size
    fun atCorrespondingPosition(viewPosition: Int): CropBundle = get(correspondingPosition(viewPosition))

    // ----------------Element Removal

    fun newPositionWithNewViewPosition(position: Index, viewPosition: Index): Pair<Index, Index> {
        val sizePostRemoval = lastIndex

        if (removingAtTail(position)){
            tailHash = at(position - 1).hashCode()
            return position.rotated(-1, sizePostRemoval) to viewPosition - 1
        }
        return listOf(position, 0)[position == sizePostRemoval] to viewPosition + 1
    }

    private fun removingAtTail(removePosition: Index): Boolean = removePosition == tailPosition

    fun rotateAndResetPositionTrackers(newViewPosition: Index, positionPostRemoval: Index){
        Collections.rotate(this, (newViewPosition % size) - positionPostRemoval)

        // reset position trackers
        with(indexOfFirst { it.hashCode() == tailHash }) {
            tailPosition = this
            headPosition = rotated(1, size)
        }
    }

    // --------------Page Index Retrieval

    fun pageIndex(position: Index): Index = headPosition.run {
        if (this <= position)
            position - this
        else
            lastIndex - this + position + 1
    }

    fun pageIndexFromViewPosition(viewPosition: Index): Index = pageIndex(correspondingPosition(viewPosition))
}