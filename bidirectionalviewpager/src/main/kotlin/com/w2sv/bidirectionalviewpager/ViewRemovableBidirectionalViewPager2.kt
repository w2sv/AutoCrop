package com.w2sv.bidirectionalviewpager

import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.w2sv.bidirectionalviewpager.recyclerview.ExtendedRecyclerViewAdapter
import com.w2sv.kotlinutils.delegates.Consumable

open class ViewRemovableBidirectionalViewPager2<T>(
    private val viewPager2: ViewPager2,
    private val dataSet: BidirectionalViewPagerDataSet<T>
) {

    private val onScrollStateIdleListener = Consumable<() -> Unit>()

    protected open inner class OnPageChangeCallback : ViewPager2.OnPageChangeCallback() {

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)

            if (state == ViewPager.SCROLL_STATE_IDLE)
                onScrollStateIdleListener.consume()?.invoke()
        }
    }

    /**
     * • scroll to subsequent position
     * • remove cropBundle from dataSet
     * • rotate dataSet such that it will subsequently align with the determined newViewPosition again
     * • reset preloaded views around newViewPosition
     * • update pageIndex dependent views
     */
    fun scrollToNextViewAndRemoveCurrent(dataSetPosition: Int, onNextViewShowingListener: (Int) -> Unit) {
        val subsequentViewPosition = viewPager2.currentItem + dataSet.viewPositionIncrement(dataSetPosition)

        // scroll to newViewPosition with blocked pageDependentViewUpdating
        viewPager2.setCurrentItem(subsequentViewPosition, true)

        onScrollStateIdleListener.value = {
            // postpone to next frame due to "RecyclerView: Cannot call this method in a scroll callback. Scroll callbacks mightbe run during a measure & layout pass where you cannot change theRecyclerView data. Any method call that might change the structureof the RecyclerView or the adapter contents should be postponed tothe next frame"
            viewPager2.post {
                // remove cropBundle from dataSet, rotate dataSet and reset position trackers such that
                // aligning with newViewPosition
                dataSet.removeAndRealign(dataSetPosition, subsequentViewPosition)

                // reset surrounding views
                (viewPager2.adapter as ExtendedRecyclerViewAdapter).resetCachedViewsAround(subsequentViewPosition)

                // update currentPosition
                onNextViewShowingListener(subsequentViewPosition)
            }
        }
    }
}