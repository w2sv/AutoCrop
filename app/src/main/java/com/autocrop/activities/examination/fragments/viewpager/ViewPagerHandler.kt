package com.autocrop.activities.examination.fragments.viewpager

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.uielements.CubeOutPageTransformer
import com.autocrop.uielements.crossFade
import com.autocrop.uielements.show
import com.autocrop.utils.Index
import com.autocrop.utils.android.*
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentViewpagerBinding
import java.util.*

class ViewPagerHandler(
    private val binding: ActivityExaminationFragmentViewpagerBinding,
    private val viewModel: ViewPagerFragmentViewModel,
    private val examinationActivity: ExaminationActivity){

    val scroller: Scroller = Scroller()
    private val pageChangeHandler = PageChangeHandler()

    init {
        // ----------init viewPager
        binding.viewPager.apply {

            // set adapter + first view
            adapter = CropPagerAdapter()

            setCurrentItem(
                viewModel.startPosition,
                false
            )

            // register onPageChangeCallbacks
            registerOnPageChangeCallback(pageChangeHandler)
        }

        // -----------init other UI elements

        // display pageIndicationSeekBar if applicable
        if (viewModel.dataSet.size > 1)
            examinationActivity.runOnUiThread { binding.pageIndicationSeekBar.show() }

        // run Scroller and display respective text view if applicable;
        // otherwise display discardedTextView and set page transformer
        if (viewModel.conductAutoScroll) {
            examinationActivity.runOnUiThread { binding.autoScrollingTextView.show() }
            scroller.run(binding.viewPager, viewModel.dataSet.size)
        }
        else{
            examinationActivity.runOnUiThread { binding.discardedTextView.show() }
            binding.viewPager.setPageTransformer()
        }
    }

    private fun ActivityExaminationFragmentViewpagerBinding.updatePageDependentViews(dataSetPosition: Index, onRightScroll: Boolean = false) =
        examinationActivity.runOnUiThread{
            discardedTextView.updateText(dataSetPosition)

            viewModel.dataSet.pageIndex(dataSetPosition).let{ pageIndex ->
                toolbar.pageIndication.updateText(pageIndex)
                pageIndicationSeekBar.update(pageIndex, onRightScroll)
            }
        }

    private fun ViewPager2.setPageTransformer() = setPageTransformer(CubeOutPageTransformer())

    private inner class PageChangeHandler: ViewPager2.OnPageChangeCallback(){
        private var previousPosition = viewModel.startPosition
        var updateViews = true

        /**
         * [updatePageDependentViews] if not blocked
         */
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            if (updateViews)
                binding.updatePageDependentViews(viewModel.dataSet.correspondingPosition(position), position > previousPosition)
            previousPosition = position
        }

        var setPageTransformerOnNextScrollCompletion = false
        var removeView: (() -> Unit)? = null

        /**
         * [setPageTransformer] upon next scroll completion if applicable
         */
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)

            if (state == ViewPager.SCROLL_STATE_IDLE){
                if (setPageTransformerOnNextScrollCompletion){
                    binding.viewPager.setPageTransformer()
                    setPageTransformerOnNextScrollCompletion = false
                }
                removeView?.let {
                    it()
                    removeView = null
                }
            }
        }
    }

    /**
     * Class accounting for automatic scrolling at the start of crop examination
     */
    inner class Scroller {
        private var timer: Timer? = null
        val isRunning: Boolean
            get() = timer != null

        fun run(viewPager2: ViewPager2, maxScrolls: Int) {
            timer = Timer().apply {
                schedule(
                    object : TimerTask() {
                        private var conductedScrolls: Int = 0

                        override fun run() {
                            Handler(Looper.getMainLooper()).post {
                                with(viewPager2) {
                                    setCurrentItem(currentItem + 1, true)
                                }
                                conductedScrolls++

                                if (conductedScrolls == maxScrolls)
                                    return@post cancel(false)
                            }
                        }
                    },
                    1000L,
                    1000L
                )
            }
        }

        /**
         * • Cancel [timer] and null it
         * • cross fade views to be displayed during scrolling and after it respectively
         * • [setPageTransformer]
         * • [displaySnackbar] if [onScreenTouch]
         */
        fun cancel(onScreenTouch: Boolean) {
            timer!!.cancel()
            timer = null

            examinationActivity.runOnUiThread {
                crossFade(binding.discardedTextView, binding.autoScrollingTextView, 900L)
            }

            if (onScreenTouch){
                binding.viewPager.setPageTransformer()
                examinationActivity.displaySnackbar(
                    "Cancelled auto scrolling",
                    NotificationColor.SUCCESS,
                    Snackbar.LENGTH_SHORT
                )
            }
            else
                pageChangeHandler.setPageTransformerOnNextScrollCompletion = true
        }
    }

    private inner class CropPagerAdapter: RecyclerView.Adapter<CropPagerAdapter.CropViewHolder>() {

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
                        override fun onTouch(v: View?, event: MotionEvent?): Boolean = scroller.run {
                            if (isRunning)
                                false
                                    .also { cancel(true) }
                            else
                                super.onTouch(v, event)
                        }

                        init {
                            examinationActivity.supportFragmentManager.setFragmentResultListener(CropProcedureDialog.PROCEDURE_SELECTED, examinationActivity){
                                    _, bundle -> onCropProcedureSelected(bundle.getInt(CropProcedureDialog.DATA_SET_POSITION_OUT))
                            }
                        }

                        /**
                         * Invoke CropProcedureDialog
                         */
                        override fun onClick() = with(CropProcedureDialog()) {
                            arguments = bundleOf(CropProcedureDialog.DATA_SET_POSITION_IN to viewModel.dataSet.correspondingPosition(adapterPosition))
                            show(examinationActivity.supportFragmentManager)
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
         * Defines crop setting wrt [position]
         */
        override fun onBindViewHolder(holder: CropViewHolder, position: Index) {
            holder.cropView.setImageBitmap(viewModel.dataSet.atCorrespondingPosition(position).crop)
        }

        override fun getItemCount(): Int = if (viewModel.dataSet.size > 1) ViewPagerFragmentViewModel.MAX_VIEWS else 1

        /**
         * Increment nSavedCrops if applicable
         *
         * triggers activity exit if [viewModel].dataSet about to be exhausted OR
         * hide [binding].pageIndicationSeekBar AND/OR
         * removes view, procedure action has been selected for, from pager
         */
        private fun onCropProcedureSelected(dataSetPosition: Index){
            when(viewModel.dataSet.size){
                1 -> return examinationActivity.run { replaceCurrentFragmentWith(appTitleFragment, true) }
                2 -> examinationActivity.runOnUiThread { binding.pageIndicationSeekBar.hideAnimated() }
                else -> Unit
            }
            removeView(dataSetPosition)
        }

        /**
         * • scroll to subsequent position
         * • remove cropBundle from dataSet
         * • rotate dataSet such that it will subsequently align with the determined newViewPosition again
         * • reset preloaded views around newViewPosition
         * • update page dependent views
         */
        private fun removeView(dataSetPosition: Index) {
            val removingAtDataSetTail = viewModel.dataSet.removingAtTail(dataSetPosition)
            val newViewPosition = binding.viewPager.currentItem + viewModel.dataSet.viewPositionIncrement(removingAtDataSetTail)

            // scroll to newViewPosition with blocked pageDependentViewUpdating
            pageChangeHandler.updateViews = false
            binding.viewPager.setCurrentItem(newViewPosition, true)
            pageChangeHandler.updateViews = true

            pageChangeHandler.removeView = {
                // remove cropBundle from dataSet, rotate dataSet and reset position trackers such that
                // aligning with newViewPosition
                viewModel.dataSet.removeAtAndRealign(dataSetPosition, removingAtDataSetTail, newViewPosition)

                // update surrounding views
                resetViewsAround(newViewPosition)

                // update views
                binding.updatePageDependentViews(viewModel.dataSet.correspondingPosition(newViewPosition))
            }
        }

        /**
         * Triggers reloading of preloaded views surrounding the one sitting at [position]
         *
         * Recycler View preloads and cashes 3 views to either side of the one being currently displayed
         */
        private fun resetViewsAround(position: Index){
            val nPreloadedViewsToEitherSide = 3

            notifyItemRangeChanged(position - nPreloadedViewsToEitherSide, nPreloadedViewsToEitherSide * 2 + 1)
        }
    }
}