package com.autocrop.activities.examination.fragments.examination.viewpager

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.activities.examination.ViewPagerViewModel
import com.autocrop.activities.examination.fragments.examination.ExaminationFragment
import com.autocrop.activities.examination.fragments.examination.PageIndicationSeekBar
import com.autocrop.crop
import com.autocrop.cropBundleList
import com.autocrop.screenshotUri
import com.autocrop.utils.Index
import com.autocrop.utils.android.TextColors
import com.autocrop.utils.android.displaySnackbar
import com.autocrop.utils.get
import com.autocrop.utils.toInt
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.R
import java.util.*

class ViewPagerHandler(
    private val viewPager2: ViewPager2,
    private val parentActivity: ExaminationActivity,
    private val viewModel: ExaminationViewModel,
    private val textViews: ExaminationFragment.TextViews,
    private val seekBar: PageIndicationSeekBar,
    private val invokeAppTitleFragment: () -> Unit){

    var scroller: Scroller? = null
    var blockPageDependentViewUpdating = false

    init {
        seekBar.calculateProgressCoefficient()

        // instantiate adapter, display first crop
        viewPager2.apply {
            adapter = CropPagerAdapter()

            setCurrentItem(
                viewModel.viewPager.startPosition,
                false
            )

            // register onPageChangeCallbacks
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                /**
                 * Updates retentionPercentage, pageIndication and seekBar upon page change if not blocked
                 */
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (!blockPageDependentViewUpdating){
                        viewModel.viewPager.dataSet.correspondingPosition(position).let{ dataSetPosition ->
                            textViews.setRetentionPercentage(dataSetPosition)

                            viewModel.viewPager.dataSet.pageIndex(dataSetPosition).let{ pageIndex ->
                                textViews.setPageIndication(pageIndex)
                                seekBar.indicatePage(pageIndex)
                            }
                        }
                    }
                }
            })

            /**
             * Set Cube Out Page Transformer
             *
             * Reference: https://www.loginworks.com/blogs/how-to-make-awesome-transition-
             * effects-using-pagetransformer-in-android/
             */
            setPageTransformer { view: View, position: Float ->
                with(view) {
                    pivotX = listOf(0f, width.toFloat())[(position < 0f).toInt()]
                    pivotY = height * 0.5f
                    rotationY = 90f * position
                }
            }

            // instantiate Scroller if applicable
            if (viewModel.viewPager.conductAutoScroll)
                scroller = Scroller(
                    viewModel.viewPager.longAutoScrollDelay,
                    cropBundleList.lastIndex
                )
        }
    }

    /**
     * Class accounting for automatic scrolling at the start of crop examination
     */
    inner class Scroller(longAutoScrollDelay: Boolean, maxScrolls: Int) {
        var isRunning: Boolean = true
        private var conductedScrolls = 0
        private var timer: Timer = Timer().apply {
            schedule(
                object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            with(viewPager2) { setCurrentItem(currentItem + 1, true) }
                            conductedScrolls++
                        }

                        if (conductedScrolls == maxScrolls)
                            cancel(onScreenTouch = false)
                    }
                },
                listOf(1000L, 1500L)[longAutoScrollDelay],
                1000L
            )
        }

        /**
         * Cancels timer, displays Snackbar as to the cause of cancellation
         */
        fun cancel(onScreenTouch: Boolean) {
            timer.cancel()
            isRunning = false

            parentActivity.displaySnackbar(
                listOf("Traversed all crops", "Cancelled auto scrolling")[onScreenTouch],
                TextColors.successfullyCarriedOut,
                Snackbar.LENGTH_SHORT
            )
        }
    }

    inner class CropPagerAdapter: RecyclerView.Adapter<CropPagerAdapter.CropViewHolder>() {

        @SuppressLint("ClickableViewAccessibility")
        inner class CropViewHolder(view: ImageView) : RecyclerView.ViewHolder(view) {
            val cropView: ImageView = view.findViewById(R.id.slide_item_image_view_examination_activity)

            /**
             * Set onTouchListener through implementation of ImmersiveViewOnTouchListener
             */
            init {
                cropView.setOnTouchListener(
                    object : ImmersiveViewOnTouchListener() {

                        /**
                         * Cancel scroller upon touch if running
                         */
                        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                            scroller?.run {
                                if (isRunning) {
                                    cancel(true)
                                    return false
                                }
                            }
                            return super.onTouch(v, event)
                        }

                        /**
                         * Invoke CropProcedureDialog upon click
                         */
                        override fun onClick() {
                            with(viewModel.viewPager.dataSet.correspondingPosition(adapterPosition)){
                                CropProcedureDialog(
                                    Pair(viewModel.viewPager.dataSet[this].screenshotUri, viewModel.viewPager.dataSet[this].crop),
                                    viewPager2.context
                                ) {incrementNSavedCrops -> onCropProcedureAction(this, incrementNSavedCrops)}
                                    .show(parentActivity.supportFragmentManager, "Crop procedure dialog")
                            }
                        }
                    }
                )
            }
        }

        /**
         * Defines creation of CropViewHolder
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder {
            return CropViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.slide_item_container_examination_activity, parent, false)
                        as ImageView
            )
        }

        /**
         * Defines setting of crop wrt position
         */
        override fun onBindViewHolder(holder: CropViewHolder, position: Index) {
            holder.cropView.setImageBitmap(viewModel.viewPager.dataSet.atCorrespondingPosition(position).crop)
        }

        override fun getItemCount(): Int = listOf(1, ViewPagerViewModel.MAX_VIEWS)[viewModel.viewPager.dataSet.size > 1]

        /**
         * Increment nSavedCrops if applicable, triggers activity exit if cropBundleList
         * about to be exhausted
         */
        private fun onCropProcedureAction(dataSetPosition: Index, incrementNSavedCrops: Boolean){
            if (incrementNSavedCrops)
                viewModel.incrementNSavedCrops()
            if (viewModel.viewPager.dataSet.size == 1)
                return parentActivity.appTitleFragment.value.invoke(false)

            removeView(dataSetPosition)
        }

        private fun removeView(dataSetPosition: Index) {
            val (newDataSetPosition, newViewPosition) = viewModel.viewPager.dataSet.newPositionWithNewViewPosition(dataSetPosition, viewPager2.currentItem)

            // scroll to newViewPosition with blocked pageDependentViewUpdating
            blockPageDependentViewUpdating = true
            viewPager2.setCurrentItem(newViewPosition, true)
            blockPageDependentViewUpdating = false

            // remove cropBundle from dataSet, rotate dataSet and reset position trackers such that
            // aligning with newViewPosition
            viewModel.viewPager.dataSet.removeAt(dataSetPosition)
            viewModel.viewPager.dataSet.rotateAndResetPositionTrackers(newViewPosition, newDataSetPosition)

            // update surrounding views
            updateViewsAround(newViewPosition)

            // update page dependent views
            viewModel.viewPager.dataSet.pageIndexFromViewPosition(newViewPosition).let{ newPageIndex ->

                textViews.apply {
                    setRetentionPercentage(viewModel.viewPager.dataSet.correspondingPosition(newViewPosition))
                    setPageIndication(newPageIndex, viewModel.viewPager.dataSet.size)
                }

                seekBar.apply {
                    calculateProgressCoefficient(viewModel.viewPager.dataSet.size)
                    indicatePage(newPageIndex)
                }
            }
        }

        private fun updateViewsAround(position: Index){
            val resetMargin = 3

            notifyItemRangeChanged(position - resetMargin, resetMargin * 2 + 1)
        }
    }
}