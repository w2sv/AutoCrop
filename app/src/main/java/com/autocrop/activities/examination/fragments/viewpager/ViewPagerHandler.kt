package com.autocrop.activities.examination.fragments.viewpager

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateInterpolator
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.global.UserPreferences
import com.autocrop.utils.Index
import com.autocrop.utils.android.*
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentViewpagerBinding
import java.util.*

class ViewPagerHandler(
    private val binding: ActivityExaminationFragmentViewpagerBinding,
    private val viewModel: ViewPagerFragmentViewModel){

    private val activity: ExaminationActivity = binding.viewPager.context as ExaminationActivity

    private val sharedViewModel: ExaminationViewModel
        get() = activity.sharedViewModel

    val scroller: Scroller = Scroller()

    private var blockPageDependentViewUpdating = false
    private var switchPageTransformerOnNextScrollStateChange = false

    init {
        binding.viewPager.apply {
            // instantiate adapter, set first crop
            adapter = CropPagerAdapter()

            setCurrentItem(
                viewModel.startPosition,
                false
            )

            if (viewModel.dataSet.size > 1)
                activity.runOnUiThread { binding.pageIndicationSeekBar.show() }

            // register onPageChangeCallbacks
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                private var previousPosition = viewModel.startPosition

                /**
                 * Updates retentionPercentage, pageIndication and seekBar upon page change if not blocked
                 */
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (!blockPageDependentViewUpdating)
                        binding.updatePageDependentViews(viewModel.dataSet.correspondingPosition(position), position > previousPosition)
                    previousPosition = position
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)

                    if (switchPageTransformerOnNextScrollStateChange && state == ViewPager.SCROLL_STATE_IDLE){
                        setCubeOutPageTransformer()
                        switchPageTransformerOnNextScrollStateChange = false
                    }
                }
            })
        }

        // run Scroller if applicable
        if (viewModel.conductAutoScroll) {
            activity.runOnUiThread { binding.autoScrollingTextView.show() }
            scroller.run(binding.viewPager, viewModel.dataSet.size)
        }
        else{
            activity.runOnUiThread { binding.discardedTextView.show() }
            setCubeOutPageTransformer()
        }
    }

    private fun setCubeOutPageTransformer(){ binding.viewPager.setPageTransformer(CubeOutPageTransformer())}

    private fun ActivityExaminationFragmentViewpagerBinding.updatePageDependentViews(dataSetPosition: Index, onRightScroll: Boolean = false) =
        activity.runOnUiThread{
            discardedTextView.updateText(dataSetPosition)

            viewModel.dataSet.pageIndex(dataSetPosition).let{ pageIndex ->
                toolbar.pageIndication.updateText(pageIndex)
                pageIndicationSeekBar.update(pageIndex, onRightScroll)
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
                                if (conductedScrolls < maxScrolls) {
                                    with(viewPager2) {
                                        setCurrentItem(currentItem + 1, true)
                                    }
                                    conductedScrolls++
                                } else
                                    cancel(onScreenTouch = false)
                            }
                        }
                    },
                    1000L,
                    1000L
                )
            }
        }

        /**
         * Cancels timer, displays Snackbar as to the cause of cancellation
         */
        fun cancel(onScreenTouch: Boolean) {
            timer!!.cancel()
            timer = null

            activity.runOnUiThread {
                crossFade(binding.discardedTextView, binding.autoScrollingTextView, 900L)
            }

            if (onScreenTouch){
                setCubeOutPageTransformer()
                activity.displaySnackbar(
                    "Cancelled auto scrolling",
                    TextColors.SUCCESS,
                    Snackbar.LENGTH_SHORT
                )
            }
            else
                switchPageTransformerOnNextScrollStateChange = true
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
                        override fun onTouch(v: View?, event: MotionEvent?): Boolean = scroller.run {
                            if (isRunning)
                                false
                                    .also { cancel(true) }
                            else
                                super.onTouch(v, event)
                        }

                        /**
                         * Invoke CropProcedureDialog upon click
                         */
                        override fun onClick() {
                            viewModel.dataSet.correspondingPosition(adapterPosition).let{ dataSetPosition ->
                                CropProcedureDialog(
                                    viewModel.dataSet[dataSetPosition].screenshotUri to viewModel.dataSet[dataSetPosition].crop,
                                    activity.contentResolver
                                )
                                {incrementNSavedCrops -> onCropProcedureAction(dataSetPosition, incrementNSavedCrops)}
                                    .show(activity.supportFragmentManager, "CROP_PROCEDURE_DIALOG")
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
         * Defines crop setting wrt [position]
         */
        override fun onBindViewHolder(holder: CropViewHolder, position: Index) {
            holder.cropView.setImageBitmap(viewModel.dataSet.atCorrespondingPosition(position).crop)
        }

        override fun getItemCount(): Int = if (viewModel.dataSet.size > 1) ViewPagerFragmentViewModel.MAX_VIEWS else 1

        /**
         * Increment nSavedCrops if applicable & triggers activity exit if cropBundleList
         * about to be exhausted; otherwise removes current view, the procedure action has been
         * selected for, from pager
         */
        private fun onCropProcedureAction(dataSetPosition: Index, incrementNSavedCrops: Boolean){
            if (incrementNSavedCrops)
                sharedViewModel.incrementImageFileIOCounters(UserPreferences.deleteIndividualScreenshot)

            when(viewModel.dataSet.size){
                1 -> return activity.run { replaceCurrentFragmentWith(appTitleFragment, true) }
                2 -> activity.runOnUiThread {
                    binding.pageIndicationSeekBar.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .setInterpolator(AnticipateInterpolator())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                binding.pageIndicationSeekBar.remove()
                            }
                        })
                        .duration = 700L
                }
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
            blockPageDependentViewUpdating = true
            binding.viewPager.setCurrentItem(newViewPosition, true)
            blockPageDependentViewUpdating = false

            // remove cropBundle from dataSet, rotate dataSet and reset position trackers such that
            // aligning with newViewPosition
            viewModel.dataSet.removeAtAndRealign(dataSetPosition, removingAtDataSetTail, newViewPosition)

            // update surrounding views
            resetViewsAround(newViewPosition)

            // update views
            binding.updatePageDependentViews(viewModel.dataSet.correspondingPosition(newViewPosition))
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