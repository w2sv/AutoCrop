package com.autocrop.activities.examination.fragments.viewpager

import androidx.annotation.VisibleForTesting
import com.autocrop.utils.rotatedIndex
import com.autocrop.utils.toInt
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
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var tailPosition: Int = lastIndex
    private val headPosition: Int get() = (tailPosition + 1) % size

    // ----------------Position Conversion

    fun correspondingPosition(viewPosition: Int): Int = viewPosition % size
    fun atCorrespondingPosition(viewPosition: Int): T = get(correspondingPosition(viewPosition))

    // ----------------Element Removal

    fun removingAtTail(removePosition: Int): Boolean = tailPosition == removePosition
    fun viewPositionIncrement(removingAtTail: Boolean): Int = if (removingAtTail) -1 else 1

    /**
     * Remove element at [removePosition]
     * rotate Collection such as to realign cropBundle sitting at [targetViewPosition] and [targetViewPosition]
     * reset [tailPosition]
     *
     * Retrieval of cropBundle at [targetViewPosition] and tail after element removal carried out by
     * storing respective hash to then look it up again -> code simplicity over efficiency
     */
    fun removeAtAndRealign(removePosition: Int, removingAtTail: Boolean, targetViewPosition: Int){
        val currentPreRemoval = correspondingPosition(targetViewPosition)

        removeAt(removePosition)

        val rotationDistance = correspondingPosition(targetViewPosition) - currentPreRemoval + (!removingAtTail).toInt()

        Collections.rotate(this, rotationDistance)
        tailPosition = rotatedIndex(tailPosition, rotationDistance - 1)
    }

    // --------------Page Index Retrieval

    fun pageIndex(position: Int): Int =
        if (position > tailPosition)
            position - headPosition
        else
            position + lastIndex - tailPosition
}