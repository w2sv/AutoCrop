package com.w2sv.bidirectionalviewpager.viewpager

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.w2sv.bidirectionalviewpager.BidirectionalViewPagerDataSet
import com.w2sv.bidirectionalviewpager.recyclerview.ExtendedRecyclerViewAdapter

/**
 * • scroll to subsequent position
 * • remove cropBundle from dataSet
 * • rotate dataSet such that it will subsequently align with the determined newViewPosition again
 * • reset preloaded views around newViewPosition
 * • update pageIndex dependent views
 */
fun ViewPager2.makeRemoveView(dataSetPosition: Int, dataSet: BidirectionalViewPagerDataSet<*>): () -> Unit {
    val subsequentViewPosition = currentItem + dataSet.viewPositionIncrement(dataSetPosition)

    // scroll to newViewPosition with blocked pageDependentViewUpdating
    dataSet.livePosition.blockSubsequentUpdate()
    setCurrentItem(subsequentViewPosition, true)

    return {
        post {  // postpone to next frame due to "RecyclerView: Cannot call this method in a scroll callback. Scroll callbacks mightbe run during a measure & layout pass where you cannot change theRecyclerView data. Any method call that might change the structureof the RecyclerView or the adapter contents should be postponed tothe next frame"
            // remove cropBundle from dataSet, rotate dataSet and reset position trackers such that
            // aligning with newViewPosition
            dataSet.removeAndRealign(dataSetPosition, subsequentViewPosition)

            // reset surrounding views
            (adapter as ExtendedRecyclerViewAdapter).resetCachedViewsAround(subsequentViewPosition)

            // update currentPosition
            dataSet.livePosition.update(subsequentViewPosition)
        }
    }
}

val ViewPager2.recyclerView: RecyclerView
    get() = getChildAt(0) as RecyclerView

@Suppress("UNCHECKED_CAST")
fun <VH : RecyclerView.ViewHolder> ViewPager2.currentViewHolder(): VH? =
    (recyclerView.findViewHolderForAdapterPosition(currentItem) as? VH)