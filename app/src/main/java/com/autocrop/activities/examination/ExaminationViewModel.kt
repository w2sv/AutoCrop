package com.autocrop.activities.examination

import androidx.lifecycle.ViewModel
import com.autocrop.CropBundle
import com.autocrop.CropBundleList
import com.autocrop.cropBundleList
import com.autocrop.utils.Index
import com.autocrop.utils.at
import com.autocrop.utils.rotated
import com.autocrop.utils.get
import java.util.*
import kotlin.properties.Delegates


class ExaminationViewModel(val conductAutoScroll: Boolean,
                           val longAutoScrollDelay: Boolean): ViewModel(){

    companion object {
        const val MAX_VIEWS: Int = Int.MAX_VALUE
    }

    var nSavedCrops = 0
    fun incrementNSavedCrops(by: Int = 1){
        nSavedCrops += by
    }

    val cropBundleList = ExtendedCropBundleList()

    val startPosition: Int = (MAX_VIEWS / 2).run {
        minus(cropBundleList.correspondingPosition(this))
    }

    data class ExtendedCropBundleList(
        var tailHash: Int = cropBundleList.last().hashCode(),
        var tailPosition: Index = cropBundleList.lastIndex,
        var headPosition: Index = 0,

        var removePosition: Index? = null) : CropBundleList by cropBundleList {

        fun correspondingPosition(pagerPosition: Int): Int = pagerPosition % size
        fun atCorrespondingPosition(pagerPosition: Int): CropBundle = get(correspondingPosition(pagerPosition))

        var replacementViewPosition by Delegates.notNull<Index>()

        val itemToBeRemoved: Boolean
            get() = removePosition != null

        private val removingAtTail: Boolean
            get() = removePosition!! == tailPosition

        val sizePostRemoval: Int
            get() = lastIndex

        private var rotationDistance by Delegates.notNull<Int>()

        fun removeElement() {
            removeAt(removePosition!!)

            Collections.rotate(this, rotationDistance)
            resetPositions()
        }

        private fun resetPositions() {
            with(indexOfFirst { it.hashCode() == tailHash }) {
                tailPosition = this
                headPosition = rotated(1, size)
            }

            removePosition = null
        }

        fun pageIndex(cropBundlePosition: Index): Index = headPosition.run {
            if (this <= cropBundlePosition)
                cropBundlePosition - this
            else
                lastIndex - this + cropBundlePosition + 1
        }

        fun replacementItemPositionPostRemoval(currentViewPagerItem: Int): Index {
            if (removingAtTail){
                replacementViewPosition = currentViewPagerItem - 1
                tailHash = at(removePosition!! - 1).hashCode()

                return removePosition!!.rotated(-1, sizePostRemoval)
            }
            replacementViewPosition = currentViewPagerItem + 1
            return listOf(removePosition!!, 0)[removePosition!! == lastIndex]
        }

        fun setNewRotationDistance(replacementItemPositionPostRemoval: Index){
            (replacementViewPosition % sizePostRemoval) - replacementItemPositionPostRemoval
        }

        fun newPageIndex(): Int = pageIndex(removePosition!!).run {
            if (removingAtTail)
                minus(1)
            this
        }
    }
}