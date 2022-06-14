package com.autocrop.activities.examination.fragments.viewpager

import com.autocrop.utils.rotated
import com.autocrop.utilsandroid.livedata.MutableListLiveData
import com.autocrop.utilsandroid.livedata.UpdateBlockableLiveData
import java.util.*

class BidirectionalViewPagerDataSet<T>(dataSet: MutableList<T>) :
    MutableListLiveData<T>(dataSet) {

    val currentPosition = UpdateBlockableLiveData(0, convertUpdateValue = ::correspondingPosition)
    val currentValue: T get() = get(currentPosition.value!!)

    /**
     * For keeping track of actual order
     */
    private var tailPosition: Int = lastIndex
    private val headPosition: Int get() = (tailPosition + 1) % size

    // ----------------Position Conversion

    fun correspondingPosition(viewPosition: Int): Int = viewPosition % size
    fun atCorrespondingPosition(viewPosition: Int): T = get(correspondingPosition(viewPosition))

    // ----------------Element Removal

    fun removingAtTail(removePosition: Int): Boolean = tailPosition == removePosition
    fun viewPositionIncrement(removingAtTail: Boolean): Int = if (removingAtTail) -1 else 1

    /**
     * Remove element at [removePosition]
     * rotate Collection such as to realign cropBundle sitting at [viewPosition] and [viewPosition]
     * reset [tailPosition]
     *
     * Retrieval of cropBundle at [viewPosition] and tail after element removal carried out by
     * storing respective hash to then look it up again -> code simplicity over efficiency
     */
    fun removeAtAndRealign(removePosition: Int, removingAtTail: Boolean, viewPosition: Int){
        val hashAtViewPosition = atCorrespondingPosition(viewPosition).hashCode()
        val subsequentTailPosition = if (removingAtTail) tailPosition.rotated(-1, size) else tailPosition
        val subsequentTailHash = get(subsequentTailPosition).hashCode()

        removeAt(removePosition)

        // rotate collection
        Collections.rotate(
            this,
            correspondingPosition(viewPosition) - indexOfFirst { it.hashCode() == hashAtViewPosition })
        tailPosition = indexOfFirst { it.hashCode() == subsequentTailHash }
    }

    // --------------Page Index Retrieval

    fun pageIndex(position: Int): Int =
        if (position > tailPosition)
            position - headPosition
        else
            position + lastIndex - tailPosition
}