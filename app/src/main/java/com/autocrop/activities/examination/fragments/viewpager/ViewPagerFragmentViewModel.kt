package com.autocrop.activities.examination.fragments.viewpager

import androidx.lifecycle.ViewModel
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.global.UserPreferences
import com.autocrop.types.CropBundle
import com.autocrop.types.CropBundleList
import com.autocrop.utils.Index
import com.autocrop.utils.at
import java.util.*
import kotlin.math.roundToInt

class ViewPagerFragmentViewModel: ViewModel(){

    companion object {
        const val MAX_VIEWS: Int = Int.MAX_VALUE
    }

    val dataSet = ViewPagerDataSet()
    val pageIndicationSeekBar = PageIndicationSeekBarModel(dataSet)

    val conductAutoScroll = UserPreferences.conductAutoScrolling && dataSet.size > 1

    val startPosition: Int = (MAX_VIEWS / 2).run {
        minus(dataSet.correspondingPosition(this))
    }
}

class ViewPagerDataSet : CropBundleList by ExaminationActivity.cropBundles {

    // -------------Position Trackers

    private var tailPosition: Index = lastIndex

    // ----------------Position Conversion

    fun correspondingPosition(viewPosition: Int, dataSetSize: Int = size): Int = viewPosition % dataSetSize
    fun atCorrespondingPosition(viewPosition: Int): CropBundle = get(correspondingPosition(viewPosition))

    // ----------------Element Removal

    fun newViewPosition(position: Index, viewPosition: Index): Index =
        if (removingAtTail(position))
            viewPosition - 1
        else
            viewPosition + 1

    private fun removingAtTail(removePosition: Index): Boolean = removePosition == tailPosition

    fun rotateAndResetPositionTrackers(newViewPosition: Index){
        val tailHash: Int = get(minOf(tailPosition, lastIndex)).hashCode()

        // rotate collection
        Collections.rotate(this, correspondingPosition(newViewPosition) - correspondingPosition(newViewPosition, size + 1))

        // rotate position tracker indices
        tailPosition = indexOfFirst { it.hashCode() == tailHash }
        println("tailPosition: $tailPosition")
    }

    // --------------Page Index Retrieval

    fun pageIndex(position: Index): Index{
        println("position: $position")

        if (position > tailPosition)
            return position - (tailPosition + 1)
        else
            return position + lastIndex - tailPosition
    }
}

class PageIndicationSeekBarModel(private val viewPagerDataSet: ViewPagerDataSet) {

    fun pagePercentage(pageIndex: Int, max: Int): Int =
        if (viewPagerDataSet.size == 1)
            50
        else
            (max.toFloat() / (viewPagerDataSet.lastIndex).toFloat() * pageIndex).roundToInt()
}