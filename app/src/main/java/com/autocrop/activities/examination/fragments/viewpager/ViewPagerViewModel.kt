package com.autocrop.activities.examination.fragments.viewpager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.collections.CropBundle
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.utils.BlankFun
import com.autocrop.utils.android.mutableLiveData
import com.autocrop.utils.rotated
import java.util.*
import kotlin.math.roundToInt

class ViewPagerViewModel:
    ViewModel(){

    val dataSet = ViewPagerDataSet(ExaminationActivityViewModel.cropBundles)

    private var scrollStateIdleListener: BlankFun? = null
    fun setScrollStateIdleListener(f: BlankFun){
        scrollStateIdleListener = f
    }
    fun consumeScrollStateIdleListenerIfSet(){
        scrollStateIdleListener?.let {
            it()
            scrollStateIdleListener = null
        }
    }

    val scrolledRight: LiveData<Boolean> by lazy {
        MutableLiveData(true)
    }

    private var blockSubsequentPageRelatedViewsUpdate = false
    fun blockSubsequentPageRelatedViewsUpdate(){
        blockSubsequentPageRelatedViewsUpdate = true
    }

    fun setDataSetPosition(viewPosition: Int, onScrollRight: Boolean? = null){
        if (!blockSubsequentPageRelatedViewsUpdate){
            onScrollRight?.let { scrolledRight.mutableLiveData.postValue(it) }
            dataSet.position.mutableLiveData.postValue(dataSet.correspondingPosition(viewPosition))
        }
        else
            blockSubsequentPageRelatedViewsUpdate = false
    }

    fun maxScrolls(): Int =
        dataSet.size - dataSet.position.value!!

    // -------------Additional parameters

    companion object { const val MAX_VIEWS: Int = Int.MAX_VALUE }

    val autoScrolledInitially = BooleanUserPreferences.conductAutoScrolling && dataSet.size > 1
    val autoScroll: MutableLiveData<Boolean> = MutableLiveData(autoScrolledInitially)

    fun initialViewPosition(): Int = (MAX_VIEWS / 2).run {
        minus(dataSet.correspondingPosition(this)) + dataSet.position.value!!
    }

    // -------------pageIndicationSeekbar

    fun pageIndicationSeekbarPagePercentage(pageIndex: Int, max: Int): Int? =
        if (dataSet.size == 1)
            null
        else
            (max.toFloat() / (dataSet.lastIndex).toFloat() * pageIndex).roundToInt()
}

class ViewPagerDataSet(private val cropBundles: MutableList<CropBundle>) :
    MutableList<CropBundle> by cropBundles, LiveData<List<CropBundle>>(){

    val position: LiveData<Int> by lazy {
        MutableLiveData(0)
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