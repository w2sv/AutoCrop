package com.autocrop.activities.examination.fragments.viewpager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.collections.CropBundle
import com.autocrop.global.BooleanPreferences
import com.autocrop.utils.BlankFun
import com.autocrop.utils.Consumable
import com.autocrop.utils.rotated
import com.autocrop.utilsandroid.mutableLiveData
import java.util.*

class ViewPagerViewModel
    : ViewModel(){

    val dataSet = ViewPagerDataSet(ExaminationActivityViewModel.cropBundles)

    //$$$$$$$$$$$$$$$
    // View Removal $
    //$$$$$$$$$$$$$$$

    var scrollStateIdleListenerConsumable by Consumable<BlankFun>()

    //$$$$$$$$$$$$$
    // AutoScroll $
    //$$$$$$$$$$$$$

    val autoScroll: LiveData<Boolean> by lazy {
        MutableLiveData(
            BooleanPreferences.autoScroll && dataSet.size > 1
        )
    }

    val maxAutoScrolls: Int
        get() = dataSet.size - dataSet.currentPosition.value!!

    //$$$$$$$$
    // Views $
    //$$$$$$$$

    companion object {
        const val MAX_VIEWS: Int = Int.MAX_VALUE
    }

    fun initialViewPosition(): Int = (MAX_VIEWS / 2).let { halvedMaxViews ->
        halvedMaxViews - dataSet.correspondingPosition(halvedMaxViews) + dataSet.currentPosition.value!!
    }
}

class ViewPagerDataSet(private val cropBundles: MutableList<CropBundle>) :
    LiveData<MutableList<CropBundle>>(),
    MutableList<CropBundle> by cropBundles {

    val containsSingleElement: Boolean
        get() = size == 1

    val currentCropBundle: CropBundle
        get() = get(currentPosition.value!!)

    val currentPosition: LiveData<Int> by lazy {
        MutableLiveData(0)
    }

    private var blockSubsequentPositionUpdate = false

    fun blockSubsequentPositionUpdate(){
        blockSubsequentPositionUpdate = true
    }

    fun updatePosition(viewPosition: Int){
        if (blockSubsequentPositionUpdate)
            blockSubsequentPositionUpdate = false
        else
            currentPosition.mutableLiveData.postValue(correspondingPosition(viewPosition))
    }

    /**
     * For keeping track of #page
     */
    private var tailPosition: Int = lastIndex

    // ----------------Position Conversion

    fun correspondingPosition(viewPosition: Int): Int = viewPosition % size
    fun atCorrespondingPosition(viewPosition: Int): CropBundle = get(correspondingPosition(viewPosition))

    // ----------------Element Removal

    fun removingAtTail(position: Int): Boolean = tailPosition == position
    fun viewPositionIncrement(removingAtTail: Boolean): Int = if (removingAtTail) -1 else 1

    override fun removeAt(index: Int): CropBundle =
        cropBundles.removeAt(index)
            .also { postValue(this) }

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