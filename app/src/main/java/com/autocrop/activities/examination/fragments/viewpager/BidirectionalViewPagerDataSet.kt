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

    fun correspondingPosition(viewPosition: Int): Int =
        viewPosition % size
    fun atCorrespondingPosition(viewPosition: Int): T =
        get(correspondingPosition(viewPosition))

    // ----------------Element Removal

    private fun removingAtTail(removePosition: Int): Boolean =
        tailPosition == removePosition
    fun subsequentViewPosition(viewPosition: Int, removePosition: Int): Int =
        viewPosition + (if (removingAtTail(removePosition)) -1 else 1)

    /**
     * Remove element at [removePosition]
     * rotate Collection such as to realign cropBundle sitting at [targetViewPosition] and [targetViewPosition]
     * reset [tailPosition]
     *
     * Retrieval of cropBundle at [targetViewPosition] and tail after element removal carried out by
     * storing respective hash to then look it up again -> code simplicity over efficiency
     */
    fun removeAtAndRealign(removePosition: Int, targetViewPosition: Int){
        val positionPreRemoval = correspondingPosition(targetViewPosition).run {
            if (removePosition < this)
                minus(1)
            else
                this
        }

        removeAt(removePosition)

        val rotationDistance = correspondingPosition(targetViewPosition) - positionPreRemoval

        Collections.rotate(this, rotationDistance)
        tailPosition = rotatedIndex(newTailPosition(removePosition), rotationDistance)
    }

    private fun newTailPosition(removedPosition: Int): Int =
        if (removingAtTail(removedPosition))
            rotatedIndex(tailPosition, -1)
        else if (tailPosition > removedPosition)
            tailPosition - 1
        else
            tailPosition

    // --------------Page Index Retrieval

    fun pageIndex(position: Int): Int =
        if (position > tailPosition)
            position - headPosition
        else
            position + lastIndex - tailPosition
}