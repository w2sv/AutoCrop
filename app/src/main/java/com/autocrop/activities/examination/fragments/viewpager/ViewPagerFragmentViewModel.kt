package com.autocrop.activities.examination.fragments.viewpager

import androidx.lifecycle.ViewModel
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.global.UserPreferences
import com.autocrop.types.CropBundle
import com.autocrop.types.CropBundleList
import com.autocrop.utils.Index
import com.autocrop.utils.rotated
import java.util.*
import kotlin.math.roundToInt

class ViewPagerFragmentViewModel: ViewModel(){

    companion object {
        const val MAX_VIEWS: Int = Int.MAX_VALUE
    }

    val dataSet = ViewPagerDataSet(ExaminationViewModel.cropBundles)

    val conductAutoScroll = UserPreferences.conductAutoScrolling && dataSet.size > 1

    val startPosition: Int = (MAX_VIEWS / 2).run {
        minus(dataSet.correspondingPosition(this))
    }

    fun pageIndicationSeekbarPagePercentage(pageIndex: Int, max: Int): Int =
        if (dataSet.size == 1)
            50
        else
            (max.toFloat() / (dataSet.lastIndex).toFloat() * pageIndex).roundToInt()
}

class ViewPagerDataSet(cropBundles: MutableList<CropBundle>) : MutableList<CropBundle> by cropBundles{

    // -------------Position Trackers

    var tailPosition: Index = lastIndex

    // ----------------Position Conversion

    fun correspondingPosition(viewPosition: Int): Int = viewPosition % size
    fun atCorrespondingPosition(viewPosition: Int): CropBundle = get(correspondingPosition(viewPosition))

    // ----------------Element Removal

    fun removingAtTail(position: Index): Boolean = tailPosition == position

    fun viewPositionIncrement(removingAtTail: Boolean): Int =
        if (removingAtTail) -1 else 1

    fun removeAtAndRealign(removePosition: Index, removingAtTail: Boolean, viewPosition: Index){
        val viewPositionCropBundleHash = atCorrespondingPosition(viewPosition).hashCode()
        val subsequentTailHash = get(if (removingAtTail) tailPosition.rotated(-1, size) else tailPosition).hashCode()

        removeAt(removePosition)

        // rotate collection
        Collections.rotate(this, correspondingPosition(viewPosition) - indexOfFirst { it.hashCode() == viewPositionCropBundleHash })
        tailPosition = indexOfFirst { it.hashCode() == subsequentTailHash }
    }

    // --------------Page Index Retrieval

    fun pageIndex(position: Index): Index =
        if (position > tailPosition)
            position - (tailPosition + 1)
        else
            position + lastIndex - tailPosition
}