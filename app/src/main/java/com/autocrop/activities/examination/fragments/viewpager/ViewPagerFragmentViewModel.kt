package com.autocrop.activities.examination.fragments.viewpager

import androidx.lifecycle.ViewModel
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.types.CropBundle
import com.autocrop.utils.rotated
import java.util.*
import kotlin.math.roundToInt

class ViewPagerFragmentViewModel:
    ViewModel(){

    val dataSet = ViewPagerDataSet(ExaminationActivityViewModel.cropBundles)

    // -------------Additional parameters

    companion object { const val MAX_VIEWS: Int = Int.MAX_VALUE }
    val conductAutoScroll = BooleanUserPreferences.conductAutoScrolling && dataSet.size > 1
    val startPosition: Int = (MAX_VIEWS / 2).run {
        minus(dataSet.correspondingPosition(this))
    }

    // -------------pageIndicationSeekbar

    fun pageIndicationSeekbarPagePercentage(pageIndex: Int, max: Int): Int =
        if (dataSet.size == 1)
            50
        else
            (max.toFloat() / (dataSet.lastIndex).toFloat() * pageIndex).roundToInt()
}

class ViewPagerDataSet(cropBundles: MutableList<CropBundle>) :
    MutableList<CropBundle> by cropBundles{

    // -------------Position Trackers

    /**
     * For keeping track of #page
     */
    var tailPosition: Int = lastIndex

    // ----------------Position Conversion

    fun correspondingPosition(viewPosition: Int): Int = viewPosition % size
    fun atCorrespondingPosition(viewPosition: Int): CropBundle = get(correspondingPosition(viewPosition))

    // ----------------Element Removal

    fun removingAtTail(position: Int): Boolean = tailPosition == position

    fun viewPositionIncrement(removingAtTail: Boolean): Int =
        if (removingAtTail) -1 else 1

    /**
     * Remove element at [removePosition]
     * rotate Collection such as to realign cropBundle sitting at [viewPosition] and [viewPosition]
     * reset [tailPosition]
     *
     * Retrieval of cropBundle at [viewPosition] and tail after element removal carried out by
     * storing respective hash to then look it up again -> code simplicity over efficiency
     */
    fun removeAtAndRealign(removePosition: Int, removingAtTail: Boolean, viewPosition: Int){
        val viewPositionCropBundleHash = atCorrespondingPosition(viewPosition).hashCode()
        val subsequentTailHash = get(if (removingAtTail) tailPosition.rotated(-1, size) else tailPosition).hashCode()

        removeAt(removePosition)

        // rotate collection
        Collections.rotate(this, correspondingPosition(viewPosition) - indexOfFirst { it.hashCode() == viewPositionCropBundleHash })
        tailPosition = indexOfFirst { it.hashCode() == subsequentTailHash }
    }

    // --------------Page Index Retrieval

    fun pageIndex(position: Int): Int =
        if (position > tailPosition)
            position - (tailPosition + 1)
        else
            position + lastIndex - tailPosition
}