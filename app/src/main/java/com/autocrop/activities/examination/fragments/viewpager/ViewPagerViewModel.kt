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

    var scrolledRight = false

    private var updatePageRelatedViews = true
    fun blockSubsequentPageRelatedViewsUpdate(){
        updatePageRelatedViews = false
    }

    fun setDataSetPosition(viewPosition: Int, onScrollRight: Boolean? = null){
        if (updatePageRelatedViews){
            onScrollRight?.let {
                scrolledRight = it
            }
            dataSet.position.mutableLiveData.postValue(dataSet.correspondingPosition(viewPosition))
        }
        else
            updatePageRelatedViews = true
    }

    // -------------Additional parameters

    companion object { const val MAX_VIEWS: Int = Int.MAX_VALUE }

    val autoScroll: MutableLiveData<Boolean> = MutableLiveData(
        BooleanUserPreferences.conductAutoScrolling && dataSet.size > 1
    )

    fun maxAutoScrolls(): Int =
        dataSet.size - dataSet.position.value!!

    fun initialViewPosition(): Int = (MAX_VIEWS / 2).run {
        minus(dataSet.correspondingPosition(this)) + dataSet.position.value!!
    }
}

class ViewPagerDataSet(private val cropBundles: MutableList<CropBundle>) :
    LiveData<MutableList<CropBundle>>(),
    MutableList<CropBundle> by cropBundles {

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