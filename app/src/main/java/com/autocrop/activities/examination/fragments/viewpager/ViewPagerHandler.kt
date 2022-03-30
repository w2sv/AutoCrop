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
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.UserPreferences
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ViewPagerModel
import com.autocrop.utils.Index
import com.autocrop.utils.android.*
import com.autocrop.utils.get
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentViewpagerBinding
import java.util.*
import kotlin.properties.Delegates

class ViewPagerHandler(private val binding: ActivityExaminationFragmentViewpagerBinding){

    private val activity: ExaminationActivity = binding.viewPager.context as ExaminationActivity

    private val viewModel: ViewPagerModel
        get() = activity.viewModel.viewPager

    val scroller = Scroller()

    private var blockPageDependentViewUpdating = false
    private var previousPosition: Int

    init {
        binding.viewPager.apply {
            // instantiate adapter, set first crop
            adapter = CropPagerAdapter()

            setCurrentItem(
                viewModel.startPosition,
                false
            )
            previousPosition = viewModel.startPosition


            if (viewModel.dataSet.size > 1)
                activity.runOnUiThread { binding.pageIndicationSeekBar.show() }

            // register onPageChangeCallbacks
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                /**
                 * Updates retentionPercentage, pageIndication and seekBar upon page change if not blocked
                 */
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (!blockPageDependentViewUpdating)
                        binding.updatePageDependentViews(viewModel.dataSet.correspondingPosition(position), position > previousPosition)
                    previousPosition = position
                }
            })
        }
        // run Scroller if applicable
        if (viewModel.conductAutoScroll)
            scroller.invoke()
        else{
            activity.runOnUiThread { binding.discardedTextView.show() }
            setCubeOutPageTransformer()
        }
    }

    private fun setCubeOutPageTransformer(){ binding.viewPager.setPageTransformer(CubeOutPageTransformer())}

    private fun ActivityExaminationFragmentViewpagerBinding.updatePageDependentViews(dataSetPosition: Index, onRightScroll: Boolean = false) =
        activity.runOnUiThread{
            toolbar.pageIndication.updateText(dataSetPosition)
            discardedTextView.updateText(dataSetPosition)
            pageIndicationSeekBar.update(dataSetPosition, onRightScroll)
        }

    /**
     * Class accounting for automatic scrolling at the start of crop examination
     */
    inner class Scroller {
        var isRunning: Boolean = false
        private var conductedScrolls = 0
        private val maxScrolls = viewModel.dataSet.lastIndex
        private lateinit var timer: Timer

        fun invoke(){
            isRunning = true
            activity.runOnUiThread { binding.autoScrollingTextView.show() }

            timer = Timer().apply {
                val period = 1000L

                schedule(
                    object : TimerTask() {
                        override fun run() {
                            Handler(Looper.getMainLooper()).post {
                                with(binding.viewPager) { setCurrentItem(currentItem + 1, true) }
                                conductedScrolls++
                            }

                            if (conductedScrolls == maxScrolls)
                                cancel(onScreenTouch = false)
                        }
                    },
                    period,
                    period
                )
            }
        }

        /**
         * Cancels timer, displays Snackbar as to the cause of cancellation
         */
        fun cancel(onScreenTouch: Boolean) {
            timer.cancel()
            isRunning = false
            activity.runOnUiThread {
                crossFade(binding.discardedTextView, binding.autoScrollingTextView, 900L)
                setCubeOutPageTransformer()
            }

            if (onScreenTouch)
                activity.displaySnackbar(
                    "Cancelled auto scrolling",
                    TextColors.SUCCESS,
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
                                    imageFileWritingContext = activity)
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

        override fun getItemCount(): Int = listOf(1, ViewPagerModel.MAX_VIEWS)[viewModel.dataSet.size > 1]

        /**
         * Increment nSavedCrops if applicable & triggers activity exit if cropBundleList
         * about to be exhausted; otherwise removes current view, the procedure action has been
         * selected for, from pager
         */
        private fun onCropProcedureAction(dataSetPosition: Index, incrementNSavedCrops: Boolean){
            if (incrementNSavedCrops){
                activity.viewModel.nSavedCrops++
                if (UserPreferences.deleteIndividualScreenshot)
                    activity.viewModel.nDeletedCrops++
            }

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
            val (newDataSetPosition, newViewPosition) = viewModel.dataSet.newPositionWithNewViewPosition(dataSetPosition, binding.viewPager.currentItem)

            // scroll to newViewPosition with blocked pageDependentViewUpdating
            blockPageDependentViewUpdating = true
            binding.viewPager.setCurrentItem(newViewPosition, true)
            blockPageDependentViewUpdating = false

            // remove cropBundle from dataSet, rotate dataSet and reset position trackers such that
            // aligning with newViewPosition
            viewModel.dataSet.removeAt(dataSetPosition)
            viewModel.dataSet.rotateAndResetPositionTrackers(newViewPosition, newDataSetPosition)

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